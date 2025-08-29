package com.enigma.library_app.service.impl;

import com.enigma.library_app.model.Fine;
import com.enigma.library_app.enumeration.PaymentMethod;
import com.enigma.library_app.model.Loan;
import com.enigma.library_app.repository.FineRepository;
import com.enigma.library_app.service.FineService;
import com.enigma.library_app.service.LoanService;
import com.enigma.library_app.service.PaymentService;
import com.midtrans.httpclient.error.MidtransError;
import com.midtrans.service.MidtransSnapApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MidtransSnapApi midtransSnapApi;
    private final FineService fineService;
    private final LoanService loanService;
    private final FineRepository fineRepository;

    @Override
    @Transactional
    public String createMidtransTransaction(String loanId) {
        Loan loan = loanService.findById(loanId);

        Fine fine = fineService.getOrCreateFineByLoan(loanId);

        fine.setPaymentMethod(PaymentMethod.TRANSFER);

        Map<String, Object> transactionDetails = new HashMap<>();

        String uniqueOrderId = fine.getFineId() + "-" + System.currentTimeMillis();
        transactionDetails.put("order_id", uniqueOrderId);
        transactionDetails.put("gross_amount", fine.getAmount());

        Map<String, String> customerDetails = new HashMap<>();
        customerDetails.put("first_name", loan.getMember().getName());
        customerDetails.put("email", loan.getMember().getEmail());
        customerDetails.put("phone", loan.getMember().getPhone());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transaction_details", transactionDetails);
        requestBody.put("customer_details", customerDetails);

        try {
            Map<String, Object> response = midtransSnapApi.createTransaction(requestBody).toMap();
            String paymentUrl = (String) response.get("redirect_url");

            fine.setPaymentUrl(paymentUrl);
            fineRepository.save(fine);

            return paymentUrl;
        } catch (MidtransError e) {
            throw new RuntimeException("Gagal membuat transaksi Midtrans: " + e.getMessage());
        }
    }
}
