package com.enigma.library_app.repository;

import com.enigma.library_app.model.Fine;
import com.enigma.library_app.enumeration.PaymentMethod;
import com.enigma.library_app.enumeration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, String> {
	@Query("SELECT f FROM Fine f " +
			"WHERE f.paymentStatus = 'PAID' AND " +
			"(?2 IS NULL OR YEAR(f.paidDate) = ?2) AND " +
			"(?1 IS NULL OR MONTH(f.paidDate) = ?1)")
	List<Fine> findPaidFinesByOptionalMonthYear(
			Integer month, Integer year);
	@Query("select f from Fine f where f.paymentStatus = ?3 " +
			"and (?1 IS NULL OR f.paidDate >= ?1)" +
			" and (?2 IS NULL OR f.paidDate <= ?2)")
	List<Fine> findByPaymentStatus(LocalDate startDate, LocalDate endDate, PaymentStatus paymentStatus);
	Optional<Fine> findByLoanId(String loanId);
	Optional<Fine> findByLoanIdAndPaymentMethod(String loanId, PaymentMethod paymentMethod);
}
