package com.enigma.library_app.repository;

import com.enigma.library_app.enumeration.StatusCopies;
import com.enigma.library_app.model.master.book.entity.Copy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CopyRepository extends JpaRepository<Copy, Long> {
	@Query("SELECT c FROM Copy c WHERE " +
			"(?1 IS NULL OR c.location.name = ?1) AND " +
			"(?2 IS NULL OR c.location.faculty.name = ?2) AND " +
			"c.status = 'AVAILABLE'")
	List<Copy> findByLocationNameAndFacultyName(String locationName, String facultyName);
	long countByBook_BookIdAndStatus(String bookId, StatusCopies status);
	long countByBook_BookIdAndLocation_LocationIdAndStatus(String bookId, Long locationId, StatusCopies status);
	List<Copy> findByBook_BookIdAndLocation_LocationIdAndStatus(String bookId, Long locationId, StatusCopies status);
	List<Copy> findByBook_BookIdAndStatus(String bookId, StatusCopies status);
}
