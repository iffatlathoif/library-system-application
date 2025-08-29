package com.enigma.library_app.handlers;

import com.enigma.library_app.model.User;
import com.enigma.library_app.enumeration.ConversationFlowState;
import com.enigma.library_app.dto.state.UserState;
import com.enigma.library_app.model.TelegramUser;
import com.enigma.library_app.model.Fine;
import com.enigma.library_app.enumeration.PaymentMethod;
import com.enigma.library_app.model.Loan;
import com.enigma.library_app.repository.FineRepository;
import com.enigma.library_app.repository.TelegramUserRepository;
import com.enigma.library_app.service.BookReviewService;
import com.enigma.library_app.service.LoanService;
import com.enigma.library_app.service.OtpService;
import com.enigma.library_app.service.OtpValidationService;
import com.enigma.library_app.service.AuthService;
import com.enigma.library_app.service.impl.ConversationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationHandler {

    private final ConversationStateService conversationStateService;
    private final AuthService authService;
    private final OtpService otpService;
    private final OtpValidationService otpValidationService;
    private final TelegramUserRepository telegramUserRepository;
    private final LoanService loanService;
    private final BookReviewService bookReviewService;
    private final FineRepository fineRepository;
    private final CommandHandler commandHandler;

    public Validable handle(Update update, LibraryBot bot) {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        int messageId = update.getMessage().getMessageId();
        UserState userState = conversationStateService.getState(chatId);

        switch (userState.state()) {
            case AWAITING_CREDENTIALS:
                return handleCredentialsInput(chatId, messageId, text, userState, bot);
            case AWAITING_OTP:
                return handleOtpInput(chatId, messageId, text, userState, bot);
            case AWAITING_REVIEW_TEXT:
                return handleReviewTextInput(chatId, messageId, text, userState, bot);
            case AWAITING_NEW_PASSWORD:
                return handleNewPasswordInput(chatId, messageId, text, bot);
            case AWAITING_PASSWORD_CHANGE_OTP:
                return handlePasswordChangeOtpInput(chatId, messageId, text, bot);
        }
        return null;
    }

    private Validable handleCredentialsInput(long chatId, int messageId, String text, UserState userState, LibraryBot bot) {
        bot.deleteMessage(chatId, messageId);

        String[] credentials = text.trim().split(" ", 2);
        if (credentials.length != 2) {
            conversationStateService.clearState(chatId);
            return new SendMessage(String.valueOf(chatId), "Format salah. Harap masukkan username dan password dipisahkan spasi. Proses dibatalkan.");
        }

        String username = credentials[0];
        String password = credentials[1];
        Optional<User> userOpt = authService.loginMemberViaBot(username, password);

        if (userOpt.isEmpty()) {
            conversationStateService.clearState(chatId);
            return new SendMessage(String.valueOf(chatId), "Username atau password salah. Proses dibatalkan.");
        }

        User user = userOpt.get();
        String otp = otpService.buatToken();
        otpService.sendOtpEmail(user.getEmail(), otp);

        conversationStateService.setState(chatId, ConversationFlowState.AWAITING_OTP, "user", user);

        return new SendMessage(String.valueOf(chatId), "Login berhasil! Kami telah mengirim kode OTP ke email Anda. Silakan masukkan kode OTP:");
    }

    private Validable handleOtpInput(long chatId, int messageId, String otp, UserState userState, LibraryBot bot) {
        bot.deleteMessage(chatId, messageId);

        User user = (User) userState.data().get("user");

        if (!otpValidationService.validateOtp(user.getEmail(), otp.trim())) {
            conversationStateService.clearState(chatId);
            return new SendMessage(String.valueOf(chatId), "Kode OTP salah atau kedaluwarsa. Proses dibatalkan.");
        }

        TelegramUser telegramUser = telegramUserRepository.findByChatId(chatId).orElse(new TelegramUser());
        telegramUser.setChatId(chatId);
        telegramUser.setMember(user.getMember());
        telegramUserRepository.save(telegramUser);

        String postLoginAction = (String) userState.data().get("post_login_action");
        Long copyId = (Long) userState.data().get("copyId");

        conversationStateService.clearState(chatId);

        if ("view_profile".equals(postLoginAction)) {
            log.info("Login berhasil, mengarahkan ke handleProfile untuk chatId: {}", chatId);
            return commandHandler.handleProfile(chatId); // Panggil kembali method untuk menampilkan profil
        }
        else if (copyId != null) {
            log.info("Login berhasil, memproses permintaan pinjaman untuk copyId: {}", copyId);
            try {
                loanService.requestLoan(user, copyId);
                return new SendMessage(String.valueOf(chatId), "Verifikasi OTP berhasil! Permintaan peminjaman Anda telah dicatat. Silakan temui staf perpustakaan dalam 24 jam untuk verifikasi akhir.");
            } catch (RuntimeException e) {
                return new SendMessage(String.valueOf(chatId), "Gagal memproses permintaan: " + e.getMessage());
            }
        }
        return null;
    }

    private Validable handleReviewTextInput(long chatId, int messageId, String reviewText, UserState userState, LibraryBot bot) {
        log.info("Menangani input teks review untuk chatId: {}", chatId);

        String loanId = (String) userState.data().get("loanId");
        Integer rating = (Integer) userState.data().get("rating");
        String username = (String) userState.data().get("username");

        Loan loan = loanService.findById(loanId);
        String bookId = loan.getCopy().getBook().getBookId();
        String bookTitle = loan.getCopy().getBook().getTitle();

        log.info("Menyimpan review (rating: {}) untuk buku '{}' dari user '{}'", rating, bookTitle, username);

        bookReviewService.createOrUpdateReview(username, bookId, rating, reviewText);

        Optional<Fine> fineCheck = fineRepository.findByLoanId(loanId);

        if (fineCheck.isEmpty()) {
            log.info("Tidak ada denda, memproses pengajuan pengembalian untuk loanId: {}", loanId);
            loanService.requestReturn(loanId);
        } else {
            log.info("Ada denda, status pinjaman diasumsikan sudah diubah oleh handler pembayaran.");
        }

        conversationStateService.clearState(chatId);

        Optional<Fine> optionalFine = fineRepository.findByLoanIdAndPaymentMethod(loanId, PaymentMethod.TUNAI);

        String additionalMessage = "";
        if (optionalFine.isPresent()) {
            additionalMessage = String.format(
                    "\n\n*Catatan Penting:*\n_Jangan lupa untuk membayar denda sebesar Rp %,d secara tunai kepada staf._",
                    optionalFine.get().getAmount()
            );
        }

        String confirmationText = String.format(
                "⭐ Terima kasih atas ulasan Anda untuk buku '%s'!\n\n" +
                        "✅ Pengajuan pengembalian berhasil. Silakan serahkan buku ke staf perpustakaan untuk diverifikasi.",
                bookTitle
        );

        SendMessage confirmationMessage = new SendMessage();
        confirmationMessage.setChatId(String.valueOf(chatId));
        confirmationMessage.setText(confirmationText);
        confirmationMessage.setParseMode("Markdown");

        return confirmationMessage;
    }

    private Validable handleNewPasswordInput(long chatId, int messageId, String newPassword, LibraryBot bot) {
        bot.deleteMessage(chatId, messageId);

        try {
            User user = telegramUserRepository.findByChatId(chatId).get().getMember().getUser();

            authService.changePassword(user.getUsername(), newPassword);

            conversationStateService.clearState(chatId);

            return new SendMessage(String.valueOf(chatId), "✅ Password Anda telah berhasil diubah.");

        } catch (Exception e) {
            conversationStateService.clearState(chatId);
            return new SendMessage(String.valueOf(chatId), "❌ Gagal mengubah password: " + e.getMessage());
        }
    }

    private Validable handlePasswordChangeOtpInput(long chatId, int messageId, String otp, LibraryBot bot) {
        bot.deleteMessage(chatId, messageId);

        User user = telegramUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"))
                .getMember().getUser();

        if (otpValidationService.validateOtp(user.getEmail(), otp.trim())) {
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_NEW_PASSWORD, "is_changing_password", true);

            SendMessage prompt = new SendMessage();
            prompt.setChatId(String.valueOf(chatId));
            prompt.setText("✅ Verifikasi OTP berhasil. Sekarang, silakan masukkan password baru Anda:");
            return prompt;
        } else {
            conversationStateService.clearState(chatId);
            return new SendMessage(String.valueOf(chatId), "❌ Kode OTP salah atau kedaluwarsa. Proses ganti password dibatalkan.");
        }
    }
}
