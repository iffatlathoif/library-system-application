package com.enigma.library_app.service;

import com.enigma.library_app.enumeration.StatusCopies;
import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.model.Copy;
import com.enigma.library_app.enumeration.LoanStatus;
import com.enigma.library_app.model.Loan;
import com.enigma.library_app.repository.CopyRepository;
import com.enigma.library_app.repository.LoanRepository;
import com.enigma.library_app.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
@Slf4j
public class StaffLoanService {

    private final LoanRepository loanRepository;
    private final CopyRepository copyRepository;
    private final SubscriptionService subscriptionService;
    @Lazy
    private final LibraryBot libraryBot;

    @Transactional
    public void verifyLoan(String loanId, boolean approve) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        if (loan.getStatus() != LoanStatus.REQUESTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan is not in a requested state.");
        }

        Copy copy = loan.getCopy();

        if (approve) {
            loan.setStatus(LoanStatus.ONGOING);
            copy.setStatus(StatusCopies.ON_LOAN);

            // Kirim notifikasi ke pengguna
            if (loan.getMember().getTelegramUser() != null) {
                long chatId = loan.getMember().getTelegramUser().getChatId();
                String bookTitle = copy.getBook().getTitle();
                String message = String.format(
                        "✅ Peminjaman Anda untuk buku '%s' telah disetujui!\n\n" +
                                "Harap kembalikan sebelum tanggal: %s.\n\n" +
                                "ℹ️ Catatan: Jika terlambat, Anda akan dikenakan denda Rp 15.000 per hari.",
                        bookTitle,
                        loan.getDueDate().toLocalDate().toString()
                );
                libraryBot.sendMessageToUser(chatId, message);
            }

        } else {
            loan.setStatus(LoanStatus.CANCELLED);
            copy.setStatus(StatusCopies.AVAILABLE);

            if (loan.getMember().getTelegramUser() != null) {
                long chatId = loan.getMember().getTelegramUser().getChatId();
                String bookTitle = copy.getBook().getTitle();
                String message = String.format(
                        "❌ Mohon maaf, peminjaman Anda untuk buku '%s' ditolak.",
                        bookTitle
                );
                libraryBot.sendMessageToUser(chatId, message);
            }
        }

        loanRepository.save(loan);
        copyRepository.save(copy);
    }

    @Transactional
    public void verifyReturn(String loanId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pinjaman tidak ditemukan"));

        if (loan.getStatus() != LoanStatus.RETURN_REQUESTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pinjaman ini tidak dalam status pengajuan pengembalian.");
        }

        loan.setStatus(LoanStatus.RETURNED);
        Copy copy = loan.getCopy();
        copy.setStatus(StatusCopies.AVAILABLE);

        if (loan.getMember().getTelegramUser() != null) {
            long chatId = loan.getMember().getTelegramUser().getChatId();
            String bookTitle = copy.getBook().getTitle();
            String message = String.format(
                    "✅ Pengembalian buku '%s' Anda telah dikonfirmasi. Terima kasih!",
                    bookTitle
            );
            libraryBot.sendMessageToUser(chatId, message);
        }

        String bookId = copy.getBook().getBookId();
        log.info("Buku {} telah dikembalikan, memicu notifikasi untuk subscriber.", bookId);
        subscriptionService.notifySubscribers(bookId);

        loanRepository.save(loan);
        copyRepository.save(copy);
    }
}