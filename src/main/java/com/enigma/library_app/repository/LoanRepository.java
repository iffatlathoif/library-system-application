package com.enigma.library_app.repository;

import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.model.transaction.loan.constant.LoanStatus;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String>, JpaSpecificationExecutor<Loan> {
    @Query("""
SELECT l FROM Loan l
JOIN l.member m
JOIN l.loanDetails ld
JOIN ld.copies c
JOIN c.location loc
WHERE loc.locationId = :locationId
  AND l.status = :status
""")
    Page<Loan> findReportByLocationAndStatus(
            @Param("locationId") Long locationId,
            @Param("status") LoanStatus status,
            Pageable pageable
    );
    List<Loan> findAllByStatusAndLoanDateBefore(LoanStatus loanStatus, LocalDateTime oneDayAgo);
    @Query("SELECT l FROM Loan l " +
            "LEFT JOIN FETCH l.member m " +
            "LEFT JOIN FETCH m.telegramUser " +
            "LEFT JOIN FETCH l.copy c " +
            "LEFT JOIN FETCH c.book " +
            "WHERE l.loanId = :loanId")
    Optional<Loan> findByIdWithDetails(@Param("loanId") String loanId);

    @Query("SELECT l FROM Loan l JOIN FETCH l.member m LEFT JOIN FETCH m.telegramUser JOIN FETCH l.copy c JOIN FETCH c.book " +
            "WHERE l.status = 'ONGOING' AND l.dueDate < :cutoffDate")
    List<Loan> findRelevantOngoingLoans(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT l FROM Loan l JOIN FETCH l.copy c JOIN FETCH c.book " +
            "WHERE l.member.memberId = :memberId AND l.status IN ('ONGOING', 'LATE') " +
            "ORDER BY l.dueDate ASC")
    Page<Loan> findActiveLoansByMemberId(@Param("memberId") String memberId, Pageable pageable);

    List<Loan> findAllByStatusAndReturnRequestDateBefore(LoanStatus status, LocalDateTime timestamp);

    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDateTime dueDate);

    Page<Loan> findByMember(Member member, Pageable pageable);

    @Query(value = """
            SELECT l.* 
            FROM loans l 
            JOIN loan_details ld ON l.loan_id = ld.loan_id 
            WHERE ld.book_id = :bookId
            """,
            nativeQuery = true)
    Page<Loan> findByBookId(String bookId, Pageable pageable);
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
    Page<Loan> findByDueDate(LocalDateTime dueDate, Pageable pageable);

    @Query(value = """
            SELECT l FROM Loan l JOIN l.copy c WHERE c.location.locationId = :locationId
            """)
    Page<Loan> findByLocationId(Long locationId, Pageable pageable);
}
