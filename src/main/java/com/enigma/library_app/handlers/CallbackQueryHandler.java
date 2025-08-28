package com.enigma.library_app.handlers;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.constan.ConversationFlowState;
import com.enigma.library_app.dto.state.UserState;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.BookReview;
import com.enigma.library_app.model.master.member.entity.TelegramUser;
import com.enigma.library_app.model.transaction.fine.entity.Fine;
import com.enigma.library_app.model.transaction.fine.entity.FinePrice;
import com.enigma.library_app.model.transaction.fine.enumeration.PaymentMethod;
import com.enigma.library_app.model.transaction.fine.enumeration.PaymentStatus;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.repository.FinePriceRepository;
import com.enigma.library_app.repository.FineRepository;
import com.enigma.library_app.repository.TelegramUserRepository;
import com.enigma.library_app.service.contract.book.BookReviewService;
import com.enigma.library_app.service.contract.book.BookService;
import com.enigma.library_app.service.contract.loan.LoanService;
import com.enigma.library_app.service.contract.otp.OtpService;
import com.enigma.library_app.service.contract.payment.PaymentService;
import com.enigma.library_app.service.contract.wishlist.SubscriptionService;
import com.enigma.library_app.service.impl.telegram.ConversationStateService;
import com.enigma.library_app.service.impl.telegram.MessageBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackQueryHandler {

    private final ConversationStateService conversationStateService;
    private final CommandHandler commandHandler;
    private final MessageBuilderService messageBuilderService;
    private final PaymentService paymentService;
    private final BookService bookService;
    private final BookReviewService bookReviewService;
    private final LoanService loanService;
    private final FineRepository fineRepository;
    private final TelegramUserRepository telegramUserRepository;
    private final SubscriptionService subscriptionService;
    private final OtpService otpService;
    private final FinePriceRepository finePriceRepository;

    @Transactional
    public Validable handleCallBackQuery(CallbackQuery callbackQuery, LibraryBot bot) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        MaybeInaccessibleMessage originalMessage = callbackQuery.getMessage();
        if (!(originalMessage instanceof Message message)) {
            return null;
        }

        if (data.startsWith("list_page:")) {
            int page = Integer.parseInt(data.substring("list_page:".length()).trim());
            Page<Book> bookPage = bookService.findAllBooks(PageRequest.of(page, 5));
            return messageBuilderService.buildBookPageMessage(chatId, null, bookPage, null);
        } else if (data.startsWith("search_page:")) {
            String[] parts = data.split(":", 3);
            String query = parts[1];
            int page = Integer.parseInt(parts[2]);
            Page<Book> bookPage = bookService.searchByTitle(query, PageRequest.of(page, 5));
            return messageBuilderService.buildBookPageMessage(chatId, null, bookPage, query);
        } else if (data.startsWith("detail:")) {
            String[] parts = data.split(":", 5);
            String bookId = parts[1];
            int returnPage = Integer.parseInt(parts[2]);
            String query = parts.length > 3 ? parts[3] : "";

            int copyPage = parts.length > 4 ? Integer.parseInt(parts[4]) : 0;

            return messageBuilderService.buildBookDetailPage(chatId, bookId, returnPage, query, copyPage);

        } else if (data.startsWith("back_to_list:")) {
            String[] parts = data.split(":", 3);
            int page = Integer.parseInt(parts[1]);
            String query = parts.length > 2 ? parts[2] : "";
            Page<Book> bookPage = (query.isEmpty())
                    ? bookService.findAllBooks(PageRequest.of(page, 5))
                    : bookService.searchByTitle(query, PageRequest.of(page, 5));
            return messageBuilderService.buildBookPageMessage(chatId, null, bookPage, query);
        } else if (data.startsWith("pinjam:")) {
            String copyIdStr = data.substring("pinjam:".length());
            Long copyId = Long.parseLong(copyIdStr);

            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_CREDENTIALS, "copyId", copyId);

            EditMessageCaption prompt = new EditMessageCaption();
            prompt.setChatId(chatId);
            prompt.setMessageId(messageId);
            prompt.setCaption("Untuk meminjam, silakan balas pesan ini dengan format:\n\n`username password`");
            prompt.setParseMode("Markdown");
            prompt.setReplyMarkup(null);
            return prompt;

        } else if (data.startsWith("review:")) {
            String[] parts = data.split(":", 5);
            String bookId = parts[1];
            int reviewPage = Integer.parseInt(parts[2]);
            int returnPage = Integer.parseInt(parts[3]);
            String query = parts.length > 4 ? parts[4] : "";

            Page<BookReview> reviews = bookReviewService.getReviewsByBookId(bookId, PageRequest.of(reviewPage, 3)); // Tampilkan 3 review per halaman
            return messageBuilderService.buildReviewPageMessage(chatId, messageId, bookId, returnPage, query, reviews);

        } else if (data.startsWith("mybooks_page:")) {
            int page = Integer.parseInt(data.substring("mybooks_page:".length()));

            return commandHandler.handleMyBooks(chatId, messageId, page);
        } else if (data.equals("start_login_for_profile")) {
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_CREDENTIALS, "post_login_action", "view_profile");

            EditMessageText prompt = new EditMessageText();
            prompt.setChatId(chatId);
            prompt.setMessageId(messageId);
            prompt.setText("Silakan balas pesan ini dengan format:\n\n`username password`");
            prompt.setParseMode("Markdown");
            prompt.setReplyMarkup(null);
            return prompt;
        } else if (data.startsWith("request_return:")) {
            String loanId = data.substring("request_return:".length());
            Loan loan = loanService.findById(loanId);

            if (loan.getDueDate().isBefore(LocalDateTime.now())) {
                log.info("Pinjaman {} terlambat, proses pembayaran denda dimulai.", loanId);

                FinePrice finePrice = finePriceRepository.findActiveFinePrices()
                        .orElseThrow(() -> new RuntimeException("Harga denda aktif tidak ditemukan!"));

                long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
                if (daysLate <= 0) daysLate = 1; // Minimal telat 1 hari
                BigDecimal totalFine = finePrice.getPrice().multiply(BigDecimal.valueOf(daysLate));

                Fine fine = fineRepository.findByLoanId(loanId).orElse(new Fine());

                fine.setLoanId(loanId);
                fine.setAmount(totalFine.intValue());
                fine.setPaymentStatus(PaymentStatus.PENDING);
                fineRepository.save(fine);

                log.info("Denda untuk loan {} berhasil dibuat/diperbarui dengan jumlah {}.", loanId, totalFine.intValue());

                String messageText = String.format(
                        "❗️ Anda terlambat mengembalikan buku ini dan memiliki denda sebesar *Rp %,d*.\n\n" +
                                "Silakan pilih metode pembayaran:",
                        fine.getAmount() // Ambil jumlah dari objek 'fine' yang sudah tersimpan
                );

                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> paymentRow = new ArrayList<>();
                InlineKeyboardButton cashButton = new InlineKeyboardButton("Bayar Tunai");
                cashButton.setCallbackData("pay_fine_cash:" + loanId);
                InlineKeyboardButton transferButton = new InlineKeyboardButton("Bayar via Transfer");
                transferButton.setCallbackData("pay_fine_transfer:" + loanId);
                paymentRow.add(cashButton);
                paymentRow.add(transferButton);
                rows.add(paymentRow);

                EditMessageText paymentPrompt = new EditMessageText();
                paymentPrompt.setChatId(chatId);
                paymentPrompt.setMessageId(messageId);
                paymentPrompt.setText(messageText);
                paymentPrompt.setParseMode("Markdown");
                paymentPrompt.setReplyMarkup(new InlineKeyboardMarkup(rows));
                return paymentPrompt;

            } else {
                log.info("Memulai alur pengembalian (meminta rating) untuk loanId: {}", loanId);

                Optional<TelegramUser> telegramUserOpt = telegramUserRepository.findByChatId(chatId);
                if (telegramUserOpt.isEmpty()) {
                    return new SendMessage(String.valueOf(chatId), "Terjadi kesalahan: data pengguna tidak ditemukan.");
                }
//                User user = telegramUserOpt.get().getMember().getUser();
//
//                conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "loanId", loanId);
//                conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "user", user);

                String username = telegramUserRepository.findByChatId(chatId)
                        .orElseThrow(() -> new RuntimeException("User telegram tidak ditemukan"))
                        .getMember().getUser().getUsername();
                conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "loanId", loanId);
                conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "username", username);

                EditMessageText prompt = new EditMessageText();
                prompt.setChatId(chatId);
                prompt.setMessageId(messageId);
                prompt.setText("Sebelum melanjutkan, silakan berikan rating Anda (1-5) untuk buku ini:");

                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> ratingRow = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    InlineKeyboardButton ratingButton = new InlineKeyboardButton("⭐".repeat(i));
                    ratingButton.setCallbackData(String.format("submit_rating:%s:%d", loanId, i));
                    ratingRow.add(ratingButton);
                }
                rows.add(ratingRow);
                prompt.setReplyMarkup(new InlineKeyboardMarkup(rows));

                return prompt;
            }
        } else if (data.startsWith("pay_fine_cash:")) {
            String loanId = data.substring("pay_fine_cash:".length());

            Fine fine = fineRepository.findByLoanId(loanId)
                    .orElseThrow(() -> new RuntimeException("Denda tidak ditemukan untuk dibayar tunai."));
            fine.setPaymentMethod(PaymentMethod.TUNAI);
            fineRepository.save(fine);

            log.info("Metode pembayaran tunai dipilih untuk loan {}. Melanjutkan ke rating.", loanId);

            Optional<TelegramUser> telegramUserOpt = telegramUserRepository.findByChatId(chatId);
            if (telegramUserOpt.isEmpty()) {
                return new SendMessage(String.valueOf(chatId), "Terjadi kesalahan: data pengguna tidak ditemukan.");
            }
            String username = telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new RuntimeException("User telegram tidak ditemukan"))
                    .getMember().getUser().getUsername();

            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "loanId", loanId);
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_RATING, "username", username);

            EditMessageText prompt = new EditMessageText();
            prompt.setChatId(chatId);
            prompt.setMessageId(messageId);
            prompt.setText("Baik, pembayaran akan dilakukan di Perpustakaan. Sekarang, silakan berikan rating Anda (1-5) untuk buku ini:");

            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> ratingRow = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                InlineKeyboardButton ratingButton = new InlineKeyboardButton("⭐".repeat(i));
                ratingButton.setCallbackData(String.format("submit_rating:%s:%d", loanId, i));
                ratingRow.add(ratingButton);
            }
            rows.add(ratingRow);
            prompt.setReplyMarkup(new InlineKeyboardMarkup(rows));

            return prompt;

        } else if (data.startsWith("pay_fine_transfer:")) {
            String loanId = data.substring("pay_fine_transfer:".length());
            log.info("Metode pembayaran transfer dipilih untuk loan {}. Membuat link Midtrans...", loanId);

            String paymentUrl = paymentService.createMidtransTransaction(loanId);

            String messageText = "✅ Link pembayaran berhasil dibuat. Silakan selesaikan pembayaran Anda melalui tombol di bawah ini. Setelah pembayaran berhasil, kami akan mengirimkan notifikasi.";

            InlineKeyboardButton payButton = new InlineKeyboardButton("Bayar Sekarang via Midtrans");
            payButton.setUrl(paymentUrl);

            EditMessageText paymentLinkMessage = new EditMessageText();
            paymentLinkMessage.setChatId(chatId);
            paymentLinkMessage.setMessageId(messageId);
            paymentLinkMessage.setText(messageText);
            paymentLinkMessage.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(payButton))));

            return paymentLinkMessage;

        } else if (data.startsWith("subscribe_book:")) {
            String bookId = data.substring("subscribe_book:".length());
            try {
                String memberId = telegramUserRepository.findByChatId(chatId).get().getMember().getMemberId();
                subscriptionService.createSubscription(memberId, bookId);

                AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setCallbackQueryId(callbackQuery.getId());
                answer.setText("✅ Berhasil ditambahkan ke wishlist!");
                answer.setShowAlert(true);
                bot.execute(answer);
                return null;

            } catch (Exception e) {
                AnswerCallbackQuery answer = new AnswerCallbackQuery();
                answer.setCallbackQueryId(callbackQuery.getId());
                answer.setText("❌ Gagal: " + e.getMessage());
                answer.setShowAlert(true);

                try {
                    bot.execute(answer);
                } catch (TelegramApiException apiException) {
                    log.error("Gagal mengirim notifikasi error subscribe ke Telegram", apiException);
                }

                return null;
            }

        } else if (data.startsWith("extend_loan:")) {
            String loanId = data.substring("extend_loan:".length());

            try {
                Loan extendedLoan = loanService.extendLoan(loanId);
                String bookTitle = extendedLoan.getCopy().getBook().getTitle();
                String newDueDate = extendedLoan.getDueDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

                String successMessage = String.format(
                        "✅ *Perpanjangan Berhasil!*\n\nMasa pinjam untuk buku '%s' telah diperpanjang hingga *%s*.",
                        bookTitle, newDueDate
                );

                EditMessageText confirmationMessage = new EditMessageText();
                confirmationMessage.setChatId(chatId);
                confirmationMessage.setMessageId(messageId);
                confirmationMessage.setText(successMessage);
                confirmationMessage.setParseMode("Markdown");
                confirmationMessage.setReplyMarkup(null);

                return confirmationMessage;

            } catch (Exception e) {
                String errorMessage = "❌ *Gagal Diperpanjang*\n\n" + e.getMessage();
                EditMessageText errorResponse = new EditMessageText();
                errorResponse.setChatId(chatId);
                errorResponse.setMessageId(messageId);
                errorResponse.setText(errorMessage);
                errorResponse.setParseMode("Markdown");
                errorResponse.setReplyMarkup(null);

                return errorResponse;
            }
        } else if (data.equals("change_password")) {
            User user = telegramUserRepository.findByChatId(chatId)
                    .orElseThrow(() -> new RuntimeException("User tidak ditemukan"))
                    .getMember().getUser();

            String otp = otpService.buatToken();
            otpService.sendOtpEmail(user.getEmail(), otp);
            log.info("Mengirim OTP ganti password ke email {}", user.getEmail());

            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_PASSWORD_CHANGE_OTP, "is_changing_password", true);

            EditMessageText prompt = new EditMessageText();
            prompt.setChatId(chatId);
            prompt.setMessageId(messageId);
            prompt.setText("Untuk keamanan, kami telah mengirimkan kode OTP ke email Anda. Silakan balas pesan ini dengan kode OTP tersebut:");
            prompt.setReplyMarkup(null);

            return prompt;
        } else if (data.startsWith("submit_rating:")) {
            String[] parts = data.split(":");
            String loanId = parts[1];
            int rating = Integer.parseInt(parts[2]);

            UserState currentState = conversationStateService.getState(chatId);
            String username = (String) currentState.data().get("username");

            if (username == null) {
                log.error("State 'username' hilang saat submit rating untuk chatId: {}", chatId);
                conversationStateService.clearState(chatId);
                return new SendMessage(String.valueOf(chatId), "Terjadi kesalahan sesi, silakan ulangi proses pengembalian.");
            }

            // ✅ KODE YANG BENAR: Semua state harus AWAITING_REVIEW_TEXT
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_REVIEW_TEXT, "loanId", loanId);
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_REVIEW_TEXT, "rating", rating);
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_REVIEW_TEXT, "username", username);

            // Sisa kode di bawah ini sudah benar
            EditMessageText prompt = new EditMessageText();
            prompt.setChatId(chatId);
            prompt.setMessageId(messageId);
            prompt.setText("Rating Anda: " + messageBuilderService.generateStarRating(rating) + "\n\nSekarang, silakan tulis ulasan singkat Anda (atau kirim '-' jika tidak ingin memberi ulasan):");
            prompt.setReplyMarkup(null);

            return prompt;
        } else if (data.equals("do_nothing")) {
            return null;
        }
        return null;
    }

}
