package com.enigma.library_app.service.impl.loan;

import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.model.transaction.loan.entity.LoanNotification;
import com.enigma.library_app.repository.LoanNotificationRepository;
import com.enigma.library_app.repository.LoanRepository;
import com.enigma.library_app.service.contract.loan.LoanReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanReminderServiceImpl implements LoanReminderService {

    private final LoanRepository loanRepository;
    private final LoanNotificationRepository notificationRepository;
    @Lazy
    private final LibraryBot libraryBot;

    // Dijadwalkan berjalan setiap hari jam 7 pagi
    @Scheduled(cron = "0 0 7 * * ?")
//    @Scheduled(cron = "0 * * * * ?")
    @Override
    public void sendDailyLoanReminders() {
        log.info("Memulai tugas pengiriman pengingat peminjaman harian...");
        LocalDate today = LocalDate.now();
        // Batas waktu: ambil semua pinjaman yang jatuh tempo sebelum besok lusa (untuk mencakup yang jatuh tempo besok)
        LocalDateTime cutoffDate = today.plusDays(2).atStartOfDay();

        List<Loan> relevantLoans = loanRepository.findRelevantOngoingLoans(cutoffDate);

        // Kelompokkan pinjaman berdasarkan member
        Map<Member, List<Loan>> loansByMember = relevantLoans.stream()
                .collect(Collectors.groupingBy(Loan::getMember));

        for (Map.Entry<Member, List<Loan>> entry : loansByMember.entrySet()) {
            Member member = entry.getKey();

            // Lewati jika notifikasi untuk member ini sudah dikirim hari ini
            if (notificationRepository.existsByMemberAndNotificationDate(member, today)) {
                log.info("Notifikasi untuk member {} ({}) sudah dikirim hari ini. Melewatkan.", member.getName(), member.getMemberId());
                continue;
            }

            List<String> reminderLines = new ArrayList<>();
            for (Loan loan : entry.getValue()) {
                LocalDate dueDate = loan.getDueDate().toLocalDate();
                String bookTitle = loan.getCopy().getBook().getTitle();
                String line = null;

                if (today.isEqual(dueDate.minusDays(1))) {
                    // Kasus: Jatuh tempo besok
                    line = String.format("📖 '%s' akan jatuh tempo **BESOK** (%s)", bookTitle, dueDate);
                } else if (today.isEqual(dueDate)) {
                    // Kasus: Jatuh tempo hari ini
                    line = String.format("❗️ '%s' harus dikembalikan **HARI INI** (%s)", bookTitle, dueDate);
                } else if (today.isAfter(dueDate)) {
                    // Kasus: Terlambat
                    long daysLate = ChronoUnit.DAYS.between(dueDate, today);
                    long fine = daysLate * 15000;
                    line = String.format("🚨 '%s' **TERLAMBAT** %d hari. Denda saat ini: *Rp %,d*", bookTitle, daysLate, fine);
                }

                if (line != null) {
                    reminderLines.add(line);
                }
            }

            if (!reminderLines.isEmpty()) {
                String message = "🔔 *Pengingat Peminjaman Buku*\n\n" +
                        String.join("\n", reminderLines) +
                        "\n\n_Harap segera kembalikan buku Anda untuk menghindari denda lebih lanjut._";

                // Kirim pesan jika member memiliki akun telegram yang terhubung
                if (member.getTelegramUser() != null) {
                    long chatId = member.getTelegramUser().getChatId();
                    libraryBot.sendMessageToUser(chatId, message);
                    log.info("Mengirim pengingat ke member: {}", member.getName());

                    // Simpan catatan notifikasi ke database
                    LoanNotification notification = new LoanNotification(null, member, today, message, true);
                    notificationRepository.save(notification);
                } else {
                    log.warn("Tidak bisa mengirim notifikasi ke member {} karena tidak terhubung dengan akun Telegram.", member.getName());
                }
            }
        }
        log.info("Tugas pengiriman pengingat harian selesai.");
    }

}
