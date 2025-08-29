package com.enigma.library_app.service;

import com.enigma.library_app.dto.fine.response.FineMonthlyDetailResponse;
import com.enigma.library_app.dto.fine.response.FineResponse;
import com.enigma.library_app.dto.fine.response.FineYearlyReportResponse;
import com.enigma.library_app.model.Fine;
import com.enigma.library_app.enumeration.PaymentMethod;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FineService {
	List<Fine> findAllPaidByMonthYear(Integer month, Integer year);
	FineMonthlyDetailResponse findMonthlyDetail(Integer month, Integer year);
	FineYearlyReportResponse findMonthlySummaryYear(Integer month, Integer year);
	List<FineResponse> findByDateRange(LocalDate startDate, LocalDate endDate);
	Fine getOrCreateFineByLoan(String loanId);
	void updatePaymentMethod(String loanId, PaymentMethod paymentMethod);
	void handlePaymentNotification(Map<String, Object> payload);

	Optional<Fine> findByLoanIdAndPaymentMethod(String loanId, PaymentMethod paymentMethod);
}
