package com.enigma.library_app.handlers;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.constan.ConversationFlowState;
import com.enigma.library_app.dto.book.BookRatingDto;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.BookSubscription;
import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.model.master.member.entity.TelegramUser;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.repository.TelegramUserRepository;
import com.enigma.library_app.repository.UserRepository;
import com.enigma.library_app.service.contract.book.BookService;
import com.enigma.library_app.service.contract.loan.LoanService;
import com.enigma.library_app.service.contract.wishlist.SubscriptionService;
import com.enigma.library_app.service.impl.telegram.ConversationStateService;
import com.enigma.library_app.service.impl.telegram.MessageBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandHandler {

    private final MessageBuilderService messageBuilderService;
    private final BookService bookService;
    private final LoanService loanService;
    private final TelegramUserRepository telegramUserRepository;
    private final SubscriptionService subscriptionService;
    private final ConversationStateService conversationStateService;
    private final UserRepository userRepository;

    @Lazy
    private LibraryBot libraryBot;

    public Validable handle(long chatId, String command, String[] commandParts) {
        switch (command) {
            case "/start": return handleStart(chatId);
            case "/caribuku": return handleSearchBook(chatId, commandParts);
            case "/daftarbuku": return handleListBook(chatId, 0);
            case "/topbuku": return handleTopRatedBooks(chatId);
            case "/bukusaya": return handleMyBooks(chatId, null, 0);
            case "/wishlist": return handleWishlist(chatId);
            case "/profil": return handleProfile(chatId);
            default:
                return new SendMessage(String.valueOf(chatId), "Perintah tidak dikenal.");
        }
    }


    private Validable handleStart(long chatId) {
        String message = "Selamat datang di Bot Library App! 📚\n\n"+
                "Dengan saya Staf Kesayanganmu, siap membantu segala kebutuhan literasimu! 🤖💡\n\n" +
                "Perintah yang tersedia:\n" +
                "/start - Untuk melihat semua perintah:\n" +
                "/daftarbuku - Untuk bisa melihat list Buku:\n" +
                "/profil - Untuk melihat profil Anda\n" +
                "/topbuku - Melihat 10 buku rating tertinggi\n" +
                "/wishlist - Melihat daftar keinginan \n" +
                "/caribuku <judul buku> - Untuk mencari buku\n" +
                "/bukusaya - Melihat buku yang sedang Anda pinjam\n";
        return new SendMessage(String.valueOf(chatId), message);
    }

    private Validable handleSearchBook(long chatId, String[] commandParts) {
        if (commandParts.length < 2) {
            return new SendMessage(String.valueOf(chatId), "Format salah. Gunakan: /caribuku <judul buku>");
        }
        String title = commandParts[1];
        Pageable pageable = PageRequest.of(0, 5);
        Page<Book> searchResultPage = bookService.searchByTitle(title, pageable);
        return messageBuilderService.buildBookPageMessage(chatId, null, searchResultPage, title);
    }

    private Validable handleListBook(long chatId, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Book> allBooksPage = bookService.findAllBooks(pageable);
        return messageBuilderService.buildBookPageMessage(chatId, null, allBooksPage, null);
    }

    private Validable handleTopRatedBooks(long chatId) {
        Pageable topTen = PageRequest.of(0, 10);
        Page<BookRatingDto> topRatedBooks = bookService.findTopRatedBooks(topTen);

        if (topRatedBooks.isEmpty()) {
            return new SendMessage(String.valueOf(chatId), "Belum ada buku yang memiliki rating.");
        }

        StringBuilder text = new StringBuilder("🏆 **10 Buku dengan Rating Tertinggi** 🏆\n\n");
        int rank = 1;
        for (BookRatingDto dto : topRatedBooks.getContent()) {
            String ratingStars = messageBuilderService.generateStarRating(dto.averageRating().intValue());
            text.append(String.format("%d. *%s*\n", rank++, dto.title()));
            text.append(String.format("   Rating: %.1f %s\n\n", dto.averageRating(), ratingStars));
        }

        SendMessage message = new SendMessage(String.valueOf(chatId), text.toString());
        message.setParseMode("Markdown");
        return message;
    }

    public Validable handleMyBooks(long chatId, Integer messageId, int page) {
        Optional<TelegramUser> telegramUserOpt = telegramUserRepository.findByChatId(chatId);

        if (telegramUserOpt.isEmpty() || telegramUserOpt.get().getMember() == null) {
            return new SendMessage(String.valueOf(chatId), "Anda belum login. Silakan login terlebih dahulu untuk melihat buku pinjaman Anda.");
        }

        String memberId = telegramUserOpt.get().getMember().getMemberId();
        Page<Loan> loanPage = loanService.findActiveLoansByMemberId(memberId, PageRequest.of(page, 5));

        return messageBuilderService.buildMyBooksPageMessage(chatId, messageId, loanPage);
    }

    private SendMessage handleWishlist(long chatId) {
        String memberId = telegramUserRepository.findByChatId(chatId).get().getMember().getMemberId();
        List<BookSubscription> wishlist = subscriptionService.getWishlistByMemberId(memberId);

        if (wishlist.isEmpty()) {
            return new SendMessage(String.valueOf(chatId), "Wishlist Anda kosong.");
        }

        StringBuilder text = new StringBuilder("📚 *Wishlist Anda*\n\n");
        for (int i = 0; i < wishlist.size(); i++) {
            text.append(String.format("%d. %s\n", i + 1, wishlist.get(i).getBook().getTitle()));
        }

        SendMessage message = new SendMessage(String.valueOf(chatId), text.toString());
        message.setParseMode("Markdown");
        return message;
    }

    public Validable handleProfile(long chatId) {
        Optional<TelegramUser> telegramUserOpt = telegramUserRepository.findByChatId(chatId);

        if (telegramUserOpt.isEmpty() || telegramUserOpt.get().getMember() == null) {
            conversationStateService.setState(chatId, ConversationFlowState.AWAITING_CREDENTIALS, "post_login_action", "view_profile");

            SendMessage prompt = new SendMessage(String.valueOf(chatId), "Anda belum login, Login terlebih dulu");

            InlineKeyboardButton loginButton = new InlineKeyboardButton("Login Sekarang");
            loginButton.setCallbackData("start_login_for_profile");
            prompt.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(loginButton))));

            return prompt;
        }

        Member member = telegramUserOpt.get().getMember();

        Optional<User> userOpt = userRepository.findByMember(member);

        if (userOpt.isEmpty()) {
            log.error("handleProfile GAGAL: Data User tidak ditemukan untuk Member: {}", member.getName());
            return new SendMessage(String.valueOf(chatId), "Error: Data akun user Anda tidak ditemukan.");
        }

        User user = userOpt.get();

        if (user == null) {
            return new SendMessage(String.valueOf(chatId), "Data user Anda tidak ditemukan.");
        }

        String text = String.format(
                "👤 *Profil Anda*\n\n" +
                        "**Username:** `%s`\n" +
                        "**Email:** `%s`",
                user.getUsername(), user.getEmail()
        );

        InlineKeyboardButton changePasswordButton = new InlineKeyboardButton("Ganti Password");
        changePasswordButton.setCallbackData("change_password");
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(List.of(List.of(changePasswordButton)));

        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setParseMode("Markdown");
        message.setReplyMarkup(keyboard);
        return message;
    }
}
