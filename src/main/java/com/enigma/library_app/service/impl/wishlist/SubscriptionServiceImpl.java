package com.enigma.library_app.service.impl.wishlist;

import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.BookSubscription;
import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.repository.BookRepository;
import com.enigma.library_app.repository.BookSubscriptionRepository;
import com.enigma.library_app.repository.MemberRepository;
import com.enigma.library_app.service.contract.wishlist.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final BookSubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Autowired
    @Lazy
    private LibraryBot libraryBot;

    @Override
    @Transactional
    public void createSubscription(String memberId, String bookId) throws Exception {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new Exception("Member tidak ditemukan"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new Exception("Buku tidak ditemukan"));

        if (subscriptionRepository.existsByMemberAndBook(member, book)) {
            throw new Exception("Anda sudah ada dalam daftar tunggu untuk buku ini.");
        }

        BookSubscription subscription = BookSubscription.builder()
                .member(member)
                .book(book)
                .isNotified(false)
                .build();

        subscriptionRepository.save(subscription);
        log.info("Member {} berhasil subscribe untuk buku {}", member.getName(), book.getTitle());
    }

    @Override
    public List<BookSubscription> getWishlistByMemberId(String memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member tidak ditemukan"));
        return subscriptionRepository.findByMemberAndIsNotifiedFalse(member);
    }

    @Override
    @Transactional
    public void notifySubscribers(String bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        List<BookSubscription> subscribers = subscriptionRepository.findByBookAndIsNotifiedFalse(book);
        if (subscribers.isEmpty()) {
            log.info("Tidak ada subscriber untuk buku '{}'", book.getTitle());
            return;
        }

        log.info("Mengirim notifikasi ketersediaan buku '{}' ke {} subscriber...", book.getTitle(), subscribers.size());
        String messageText = String.format(
                "🎉 *Buku Tersedia!*\n\nBuku yang Anda inginkan, '%s', kini telah tersedia untuk dipinjam. Segera pinjam sebelum kehabisan!",
                book.getTitle()
        );


        InlineKeyboardButton borrowButton = new InlineKeyboardButton("Lihat & Pinjam Sekarang 📖");
        borrowButton.setCallbackData("detail:" + book.getBookId() + ":0::0");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(List.of(List.of(borrowButton)));

        for (BookSubscription sub : subscribers) {
            Long chatId = sub.getMember().getTelegramUser().getChatId();

            SendMessage notificationMessage = new SendMessage(String.valueOf(chatId), messageText);
            notificationMessage.setParseMode("Markdown");
            notificationMessage.setReplyMarkup(keyboard);

            try {
                libraryBot.execute(notificationMessage);
                sub.setNotified(true);
            } catch (TelegramApiException e) {
                log.error("Gagal mengirim notifikasi ketersediaan ke chatId: {}", chatId, e);
            }
        }

        subscriptionRepository.saveAll(subscribers);
    }
}
