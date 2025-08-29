package com.enigma.library_app.service.impl;

import com.enigma.library_app.enumeration.ConversationFlowState;
import com.enigma.library_app.dto.fine.response.*;
import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.model.Fine;
import com.enigma.library_app.model.FinePrice;
import com.enigma.library_app.enumeration.PaymentMethod;
import com.enigma.library_app.enumeration.PaymentStatus;
import com.enigma.library_app.model.Loan;
import com.enigma.library_app.repository.FineRepository;
import com.enigma.library_app.repository.TelegramUserRepository;
import com.enigma.library_app.service.FineService;
import com.enigma.library_app.service.LoanService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FineServiceImpl implements FineService {

	private final FineRepository fineRepository;
	private final LoanService loanService;
	private final TelegramUserRepository telegramUserRepository;
	private final ConversationStateService conversationService;

	@Autowired
	@Lazy
	private LibraryBot libraryBot;

	@Override
	public List<Fine> findAllPaidByMonthYear(Integer month, Integer year) {
		return fineRepository.findPaidFinesByOptionalMonthYear(month, year);
	}

	@Override
	public FineMonthlyDetailResponse findMonthlyDetail(Integer month, Integer year) {
		List<Fine> paidFines = findAllPaidByMonthYear(month, year);
		return fineDetailsByMonth(month, year, paidFines);
	}

	@Override
	public FineYearlyReportResponse findMonthlySummaryYear(Integer month, Integer year) {
		List<Fine> paidFines = findAllPaidByMonthYear(month, year);
		return findMonthlySummaryYear(year, paidFines);
	}

	@Override
	public List<FineResponse> findByDateRange(LocalDate startDate, LocalDate endDate) {
		List<Fine> fines = fineRepository.findByPaymentStatus(startDate, endDate, PaymentStatus.PAID);
		return fines.stream().map(this::toDto).toList();
	}

	private FineResponse toDto(Fine fine) {
		return FineResponse.builder()
				.fineId(fine.getFineId())
				.loanId(fine.getLoanId())
				.amount(fine.getAmount())
				.issuedDate(fine.getIssuedDate())
				.paidDate(fine.getPaidDate())
				.status(fine.getPaymentStatus().name())
				.type(fine.getPaymentMethod().name())
				.build();
	}

	private static FineYearlyReportResponse findMonthlySummaryYear(Integer year, List<Fine> paidFines) {
		Map<Integer, List<Fine>> groupedByMonth = paidFines.stream()
				.filter(f -> f.getPaidDate() != null)
				.collect(Collectors.groupingBy(f -> f.getPaidDate().getMonthValue()));
		List<MonthlyFineSummary> monthlyFineSummaries = new ArrayList<>();
		int totalIncome = 0;
		for (int i = 1; i <= 12; i++) {
			List<Fine> monthlyFines = groupedByMonth.getOrDefault(i, new ArrayList<>());
			int totalPaid = monthlyFines.stream().mapToInt(Fine::getAmount).sum();
			int transactions = monthlyFines.size();
			totalIncome += totalPaid;
			MonthlyFineSummary monthlyFineSummary = MonthlyFineSummary.builder()
					.month(Month.of(i).name())
					.totalPaid(totalPaid)
					.transactions(transactions)
					.build();

			monthlyFineSummaries.add(monthlyFineSummary);
		}
		int yearSelected = year == null ? LocalDate.now().getYear() : year;
		return FineYearlyReportResponse.builder()
				.year(yearSelected)
				.totalIncome(totalIncome)
				.monthlyReports(monthlyFineSummaries)
				.build();
	}

	private FineMonthlyDetailResponse fineDetailsByMonth(Integer month, Integer year, List<Fine> paidFines) {
		List<FineDetailResponse> fineDetailResponses = new ArrayList<>();
		Integer total = 0;
		for (Fine fine : paidFines) {
			total += fine.getAmount();
			FineDetailResponse fineDetailResponse = FineDetailResponse.builder()
					.loanId(fine.getLoanId())
					.fineId(fine.getFineId())
					.amount(fine.getAmount())
					.issuedDate(fine.getIssuedDate())
					.paidDate(fine.getPaidDate())
					.status(fine.getPaymentStatus().name())
					.build();
			fineDetailResponses.add(fineDetailResponse);
		}
		int yearSelected = year == null ? LocalDate.now().getYear() : year;
		return FineMonthlyDetailResponse.builder()
				.year(yearSelected)
				.month(Month.of(month).name())
				.totalPaid(total)
				.transactions(paidFines.size())
				.fines(fineDetailResponses)
				.build();
	}

	@Override
	@Transactional
	public Fine getOrCreateFineByLoan(String loanId) {
		return fineRepository.findByLoanId(loanId).orElseGet(() -> {
			Loan loan = loanService.findById(loanId);
			long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDateTime.now());
			if (daysLate < 0) daysLate = 0;

			FinePrice price = new FinePrice();

			int fineAmount = price.getPrice()
					.multiply(BigDecimal.valueOf(daysLate))
					.intValue();

			Fine newFine = Fine.builder()
					.fineId(UUID.randomUUID().toString())
					.loanId(loanId)
					.amount(fineAmount)
					.paymentStatus(PaymentStatus.PENDING)
					.build();
			return fineRepository.save(newFine);
		});
	}

	@Override
	@Transactional
	public void updatePaymentMethod(String loanId, PaymentMethod paymentMethod) {
		Fine fine = getOrCreateFineByLoan(loanId);
		fine.setPaymentMethod(paymentMethod);
		fineRepository.save(fine);
		log.info("Metode pembayaran untuk denda {} diubah menjadi {}", fine.getFineId(), paymentMethod);
	}

	@Override
	@Transactional
	public void handlePaymentNotification(Map<String, Object> payload) {
		String orderIdFromMidtrans = (String) payload.get("order_id");
		String transactionStatus = (String) payload.get("transaction_status");
		String paymentType = (String) payload.get("payment_type");

		String fineId = parseFineIdFromOrderId(orderIdFromMidtrans);

		Fine fine = fineRepository.findById(fineId)
				.orElseThrow(() -> new RuntimeException("Fine not found for id: " + fineId));

		if (fine.getPaymentStatus() == PaymentStatus.PAID) {
			log.info("Denda {} sudah lunas, notifikasi diabaikan.", fineId);
			return;
		}

		String paymentProvider = paymentType;
		if ("bank_transfer".equals(paymentType) && payload.containsKey("va_numbers")) {
			List<Map<String, String>> vaNumbers = (List<Map<String, String>>) payload.get("va_numbers");
			if (!vaNumbers.isEmpty()) {
				paymentProvider = vaNumbers.get(0).get("bank") + "_va";
			}
		}
		fine.setPaymentProvider(paymentProvider);

		switch (transactionStatus) {
			case "settlement":
				fine.setPaymentStatus(PaymentStatus.PAID);

				Loan loan = loanService.findById(fine.getLoanId());
				Long chatId = loan.getMember().getTelegramUser().getChatId();

				String username = loan.getMember().getUser().getUsername();

				loanService.requestReturn(loan.getLoanId());

				conversationService.setState(chatId, ConversationFlowState.AWAITING_RATING, "loanId", loan.getLoanId());
				conversationService.setState(chatId, ConversationFlowState.AWAITING_RATING, "username", username);

				String successMessage = String.format(
						"✅ Pembayaran denda untuk buku '%s' telah berhasil.\n\n" +
								"Silakan berikan rating Anda (1-5) untuk buku ini:",
						loan.getCopy().getBook().getTitle()
				);

				SendMessage ratingPrompt = new SendMessage(String.valueOf(chatId), successMessage);
				List<List<InlineKeyboardButton>> rows = new ArrayList<>();
				List<InlineKeyboardButton> ratingRow = new ArrayList<>();
				for (int i = 1; i <= 5; i++) {
					InlineKeyboardButton ratingButton = new InlineKeyboardButton("⭐".repeat(i));
					ratingButton.setCallbackData(String.format("submit_rating:%s:%d", loan.getLoanId(), i));
					ratingRow.add(ratingButton);
				}
				rows.add(ratingRow);
				ratingPrompt.setReplyMarkup(new InlineKeyboardMarkup(rows));
				sendTelegramMessage(chatId, ratingPrompt);
				break;

			case "cancel":
			case "expire":
			case "deny":
				fine.setPaymentStatus(PaymentStatus.FAILED);
				Loan failedLoan = loanService.findById(fine.getLoanId());
				Long failedChatId = failedLoan.getMember().getTelegramUser().getChatId();
				String failureMessage = String.format(
						"❌ Pembayaran denda untuk buku '%s' gagal atau dibatalkan. " +
								"Silakan coba lagi dari menu /bukusaya.",
						failedLoan.getCopy().getBook().getTitle()
				);
				sendTelegramMessage(failedChatId, new SendMessage(String.valueOf(failedChatId), failureMessage));
				break;

			case "pending":
				fine.setPaymentStatus(PaymentStatus.PENDING);
				log.info("Pembayaran untuk denda {} sedang menunggu (pending).", fine.getFineId());
				break;
		}

		fineRepository.save(fine);
	}

	private String parseFineIdFromOrderId(String orderId) {
		if (orderId != null && orderId.length() >= 36) {
			return orderId.substring(0, 36);
		}
		return orderId;
	}

	private void sendTelegramMessage(Long chatId, SendMessage message) {
		try {
			libraryBot.execute(message);
		} catch (TelegramApiException e) {
			log.error("Gagal mengirim pesan dari service ke chatId {}: {}", chatId, e.getMessage());
		}
	}

	@Override
	public Optional<Fine> findByLoanIdAndPaymentMethod(String loanId, PaymentMethod paymentMethod) {
		return fineRepository.findByLoanIdAndPaymentMethod(loanId, paymentMethod);
	}

	private void sendTelegramMessage(Long chatId, String text) {
		SendMessage message = new SendMessage(String.valueOf(chatId), text);
		try {
			libraryBot.execute(message);
		} catch (TelegramApiException e) {
			log.error("Gagal mengirim pesan dari service ke chatId {}: {}", chatId, e.getMessage());
		}
	}
}
