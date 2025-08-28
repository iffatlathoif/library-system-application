package com.enigma.library_app.service.contract.loan;

import com.enigma.library_app.dto.loan.request.CreateLoanByMemberRequest;
import com.enigma.library_app.dto.loan.request.CreateLoanByStaffRequest;
import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.dto.loan.request.LoanReportRequest;
import com.enigma.library_app.dto.loan.request.UpdateLoanRequest;
import com.enigma.library_app.dto.loan.response.LoanReportResponse;
import com.enigma.library_app.dto.loan.response.LoanResponse;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanService {
    LoanResponse createLoanByStaff(CreateLoanByStaffRequest request);
    LoanResponse createLoanByMember(CreateLoanByMemberRequest request);
    LoanResponse verifyLoanRequest(String loanId, boolean approve);
    LoanResponse returnLoan(String loanId);
    LoanResponse update(String loanId, UpdateLoanRequest request);
    LoanResponse getById(String loanId);
    Page<LoanResponse> getAll(int page, int size);
    void requestLoan(User user, Long copyId);
    void checkExpiredLoanRequests();
    Page<Loan> findActiveLoansByMemberId(String memberId, Pageable pageable);
    Loan requestReturn(String loanId);
    Loan findById(String loanId);
    Loan extendLoan(String loanId) throws Exception;
    void confirmReturn(String loanId);
    Page<LoanResponse> getByMember(String memberId, int page, int size);
    Page<LoanResponse> getByBook(String bookId, int page, int size);
    Page<LoanResponse> getByStatus(String status, int page, int size);
    Page<LoanResponse> getByDueDate(String dueDate, int page, int size);
    Page<LoanResponse> getByLocation(Long locationId, int page, int size);
    Page<LoanReportResponse> generateLoanReport(Pageable pageable);
}
