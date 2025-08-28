package com.enigma.library_app.service.impl.telegram;

import com.enigma.library_app.enumeration.StatusCopies;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.BookReview;
import com.enigma.library_app.model.master.book.entity.Copy;
import com.enigma.library_app.model.transaction.fine.entity.FinePrice;
import com.enigma.library_app.model.transaction.loan.constant.LoanStatus;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.repository.BookRepository;
import com.enigma.library_app.repository.FinePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageBuilderService {

    private final BookRepository bookRespository;
    private final FinePriceRepository finePriceRepository;

    public Validable buildMyBooksPageMessage(long chatId, Integer messageId, Page<Loan> loanPage) {
        StringBuilder text = new StringBuilder("📚 *Buku yang Sedang Anda Pinjam*\n\n");

        Optional<FinePrice> finePrice = finePriceRepository.findActiveFinePrices();
        BigDecimal fineAmount = finePrice.get().getPrice();

        if (loanPage.isEmpty()) {
            text.append("_Anda tidak memiliki buku yang sedang dipinjam._");
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (Loan loan : loanPage.getContent()) {
            String bookTitle = loan.getCopy().getBook().getTitle();
            LocalDate dueDate = loan.getDueDate().toLocalDate();
            text.append(String.format("📖 *%s*\nJatuh Tempo: %s\n", bookTitle, dueDate));


            if (LocalDate.now().isAfter(dueDate)) {
                long daysLate = ChronoUnit.DAYS.between(dueDate, LocalDate.now());

                BigDecimal fine = new BigDecimal(daysLate).multiply(fineAmount);

                text.append(String.format("Status: *TERLAMBAT* %d hari\n", daysLate));
                text.append(String.format("Denda Saat Ini: *Rp %,d*\n", fine.longValue()));
            }
            text.append("\n");

            List<InlineKeyboardButton> actionRow = new ArrayList<>();

            if (loan.getStatus() == LoanStatus.ONGOING && !loan.isHasBeenExtended()) {
                InlineKeyboardButton extendButton = new InlineKeyboardButton("Perpanjang");
                extendButton.setCallbackData("extend_loan:" + loan.getLoanId());
                actionRow.add(extendButton);
            }

            InlineKeyboardButton returnButton = new InlineKeyboardButton("Ajukan Pengembalian");
            returnButton.setCallbackData("request_return:" + loan.getLoanId());
            actionRow.add(returnButton);

            buttons.add(actionRow);
        }

        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
        if (loanPage.hasPrevious()) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("◀️ Sebelumnya");
            prevButton.setCallbackData("mybooks_page:" + loanPage.previousOrFirstPageable().getPageNumber());
            paginationRow.add(prevButton);
        }
        if (loanPage.hasNext()) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("Berikutnya ▶️");
            nextButton.setCallbackData("mybooks_page:" + loanPage.nextOrLastPageable().getPageNumber());
            paginationRow.add(nextButton);
        }
        if (!paginationRow.isEmpty()) {
            buttons.add(paginationRow);
        }

        keyboard.setKeyboard(buttons);

        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(chatId);
            editMessage.setMessageId(messageId);
            editMessage.setText(text.toString());
            editMessage.setParseMode("Markdown");
            editMessage.setReplyMarkup(keyboard);
            return editMessage;
        } else {
            SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text.toString());
            sendMessage.setParseMode("Markdown");
            sendMessage.setReplyMarkup(keyboard);
            return sendMessage;
        }
    }

    public Validable buildBookPageMessage(long chatId, Integer messageId, Page<Book> bookPage, String query) {
        if (!bookPage.hasContent()) {
            return new SendMessage(String.valueOf(chatId), "Buku tidak ditemukan.");
        }
        String text = buildBookListText(bookPage);
        InlineKeyboardMarkup keyboardMarkup = createBookListKeyboard(bookPage, query);

        if (messageId != null) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(String.valueOf(chatId));
            editMessage.setMessageId(messageId);
            editMessage.setText(text);
            editMessage.setReplyMarkup(keyboardMarkup);
            return editMessage;
        } else {
            SendMessage message = new SendMessage(String.valueOf(chatId), text);
            message.setReplyMarkup(keyboardMarkup);
            return message;
        }
    }

    public Validable buildBookDetailPage(long chatId, String bookId, int returnPage, String query, int copyPage) {
        log.info("MEMBANGUN HALAMAN DETAIL -> bookId: {}, returnPage: {}, query: '{}', copyPage: {}", bookId, returnPage, query, copyPage);

        Optional<Book> optionalBook = bookRespository.findBookWithCopiesAndLocation(bookId);

        if (optionalBook.isEmpty()) {
            return new SendMessage(String.valueOf(chatId), "Detail buku tidak ditemukan.");
        }

        Book book = optionalBook.get();
        long totalCopies = book.getCopies().size();

        List<Copy> availableCopiesList = book.getCopies().stream()
                .filter(copy -> copy.getStatus() == StatusCopies.AVAILABLE)
                .toList();
        long availableCopiesCount = availableCopiesList.size();

        int copyPageSize = 5;
        int totalCopyPages = (int) Math.ceil((double) availableCopiesList.size() / copyPageSize);

        String pageInfoText = "";
        if (totalCopyPages > 1) {
            pageInfoText = String.format("\n\n*(Menampilkan salinan halaman %d dari %d)*", copyPage + 1, totalCopyPages);
        }

        String borrowLabel = availableCopiesCount > 0 ? "\n\n👇 **Pilih salinan untuk dipinjam** 👇" : "";

        String captionText = String.format(
                "📖 **%s**\n\n" +
                        "**ISBN:** %s\n" +
                        "**Penerbit:** %s\n" +
                        "**Tahun Terbit:** %s\n\n" +
                        "Tersedia **%d** dari **%d** total salinan.%s%s",
                book.getTitle(),
                book.getIsbn(),
                book.getPublisher() != null ? book.getPublisher().getName() : "Tidak diketahui",
                book.getPublicationYear(),
                availableCopiesCount,
                totalCopies,
                pageInfoText,
                borrowLabel
        );

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (!availableCopiesList.isEmpty()) {
            int fromIndex = copyPage * copyPageSize;
            int toIndex = Math.min(fromIndex + copyPageSize, availableCopiesList.size());
            List<Copy> copiesToShow = availableCopiesList.subList(fromIndex, toIndex);

            for (Copy copy : copiesToShow) {
                InlineKeyboardButton pinjamButton = new InlineKeyboardButton("📍 " + copy.getLocation().getName() + " (Rak: " + copy.getRackCode() + ")");
                pinjamButton.setCallbackData("pinjam:" + copy.getCopyId());
                buttons.add(List.of(pinjamButton));
            }

            List<InlineKeyboardButton> copyPaginationRow = new ArrayList<>();
            if (copyPage > 0) {
                InlineKeyboardButton prevButton = new InlineKeyboardButton("◀️ Sebelumnya");
                prevButton.setCallbackData("detail:" + bookId + ":" + returnPage + ":" + query + ":" + (copyPage - 1));
                copyPaginationRow.add(prevButton);
            }
            if (toIndex < availableCopiesList.size()) {
                InlineKeyboardButton nextButton = new InlineKeyboardButton("Berikutnya ▶️");
                nextButton.setCallbackData("detail:" + bookId + ":" + returnPage + ":" + query + ":" + (copyPage + 1));
                copyPaginationRow.add(nextButton);
            }
            if (!copyPaginationRow.isEmpty()) {
                buttons.add(copyPaginationRow);
            }

        } else {
            InlineKeyboardButton notifyButton = new InlineKeyboardButton("🔔 Beritahu Saya Jika Tersedia");
            notifyButton.setCallbackData("subscribe_book:" + bookId);
            buttons.add(List.of(notifyButton));
        }

        List<InlineKeyboardButton> bottomRow = new ArrayList<>();

        InlineKeyboardButton reviewButton = new InlineKeyboardButton("⭐ Lihat Review");
        String reviewCallback = String.format("review:%s:%d:%d:%s", bookId, 0, returnPage, query);
        reviewButton.setCallbackData(reviewCallback);
        bottomRow.add(reviewButton);

        InlineKeyboardButton backButton = new InlineKeyboardButton("◀️ Kembali ke Daftar"); // Teks dipersingkat
        String backCallback = (query == null || query.isEmpty()) ? "list_page:" + returnPage : "search_page:" + query + ":" + returnPage;
        backButton.setCallbackData(backCallback);
        bottomRow.add(backButton);

        buttons.add(bottomRow);

        keyboard.setKeyboard(buttons);

        SendPhoto photoMessage = new SendPhoto();
        photoMessage.setChatId(String.valueOf(chatId));
        photoMessage.setPhoto(new InputFile(book.getImageUrl() != null ? book.getImageUrl() : "https://placehold.co/600x400.png"));
        photoMessage.setCaption(captionText);
        photoMessage.setParseMode("Markdown");
        photoMessage.setReplyMarkup(keyboard);

        return photoMessage;
    }

    public InlineKeyboardMarkup createBookListKeyboard(Page<Book> bookPage, String query) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        String callbackQuery = (query == null || query.isEmpty()) ? "" : query;

        for (Book book : bookPage.getContent()) {
            List<InlineKeyboardButton> buttonRow = new ArrayList<>();
            InlineKeyboardButton detailButton = new InlineKeyboardButton("Lihat Detail : " + book.getTitle());
            detailButton.setCallbackData("detail:" + book.getBookId() + ":" + bookPage.getNumber() + ":" + callbackQuery);
            buttonRow.add(detailButton);
            buttons.add(buttonRow);
        }

        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
        String pageCallbackPrefix = (query == null || query.isEmpty()) ? "list_page:" : "search_page:" + query + ":";

        if (bookPage.hasPrevious()) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("◀️ Sebelumnya");
            prevButton.setCallbackData(pageCallbackPrefix + bookPage.previousOrFirstPageable().getPageNumber());
            paginationRow.add(prevButton);
        }
        if (bookPage.hasNext()) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("Berikutnya ▶️");
            nextButton.setCallbackData(pageCallbackPrefix + bookPage.nextOrLastPageable().getPageNumber());
            paginationRow.add(nextButton);
        }
        if (!paginationRow.isEmpty()) {
            buttons.add(paginationRow);
        }
        return new InlineKeyboardMarkup(buttons);
    }

    public String buildBookListText(Page<Book> bookPage) {
        StringBuilder text = new StringBuilder("Daftar Buku (Halaman " + (bookPage.getNumber() + 1) + "/" + bookPage.getTotalPages() + " ):\n\n");
        for (Book book : bookPage.getContent()) {
            text.append("📖 ").append(book.getTitle()).append("\n");
        }
        return text.toString();
    }

    public String generateStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "⭐" : "☆");
        }
        return stars.toString();
    }

    public Validable buildReviewPageMessage(long chatId, int messageId, String bookId, int returnPage, String query, Page<BookReview> reviewPage) {
        StringBuilder text = new StringBuilder();
        text.append("💬 **Review & Rating**\n");

        if (reviewPage.getTotalPages() > 1) {
            text.append(String.format("*(Halaman %d dari %d)*\n\n", reviewPage.getNumber() + 1, reviewPage.getTotalPages()));
        } else {
            text.append("\n");
        }

        if (reviewPage.isEmpty()) {
            text.append("Belum ada review untuk buku ini.");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (BookReview review : reviewPage.getContent()) {
                text.append(generateStarRating(review.getRating())).append("\n");
                text.append("_\"").append(review.getReviewText()).append("\"_\n");
                text.append("- **").append(review.getMember().getName()).append("** ");
                text.append("(").append(review.getCreatedAt().format(formatter)).append(")\n\n");
            }
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();

        if (reviewPage.hasPrevious()) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("◀️");
            String prevCallback = String.format("review:%s:%d:%d:%s", bookId, reviewPage.previousOrFirstPageable().getPageNumber(), returnPage, query);
            prevButton.setCallbackData(prevCallback);
            paginationRow.add(prevButton);
        }
        if (reviewPage.hasNext()) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("▶️");
            String nextCallback = String.format("review:%s:%d:%d:%s", bookId, reviewPage.nextOrLastPageable().getPageNumber(), returnPage, query);
            nextButton.setCallbackData(nextCallback);
            paginationRow.add(nextButton);
        }
        if (!paginationRow.isEmpty()) {
            buttons.add(paginationRow);
        }

        InlineKeyboardButton backButton = new InlineKeyboardButton("◀️ Kembali ke Detail Buku");
        backButton.setCallbackData("detail:" + bookId + ":" + returnPage + ":" + query + ":0");
        buttons.add(List.of(backButton));

        keyboard.setKeyboard(buttons);

        EditMessageCaption editMessage = new EditMessageCaption();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setCaption(text.toString());
        editMessage.setParseMode("Markdown");
        editMessage.setReplyMarkup(keyboard);

        return editMessage;
    }
}
