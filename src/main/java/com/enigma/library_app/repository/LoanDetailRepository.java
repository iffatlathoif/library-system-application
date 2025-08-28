package com.enigma.library_app.repository;

import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.model.transaction.loan.entity.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanDetailRepository extends JpaRepository<LoanDetail, String>, JpaSpecificationExecutor<LoanDetail> {
    void deleteAllByLoan_LoanId(String loanId);
    @Query("""
    SELECT l FROM Loan l
    JOIN FETCH l.copy
    JOIN FETCH l.loanDetails ld
    JOIN FETCH ld.book
    WHERE l.loanId = :loanId
""")
    Optional<Loan> findByIdWithDetails(@Param("loanId") String loanId);
}
