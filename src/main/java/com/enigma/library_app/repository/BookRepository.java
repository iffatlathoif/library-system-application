package com.enigma.library_app.repository;

import com.enigma.library_app.dto.book.BookRatingDto;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
	Page<Book> findByCategory(Category category, Pageable pageable);

	@Query(value = "SELECT b FROM Book b JOIN FETCH b.publisher",
			countQuery = "SELECT COUNT(b) FROM Book b")
	Page<Book> findAllWithPublisher(Pageable pageable);

	@Query("SELECT b FROM Book b " +
			"LEFT JOIN FETCH b.publisher " +
			"LEFT JOIN FETCH b.copies c " +
			"LEFT JOIN FETCH c.location l " +
			"LEFT JOIN FETCH l.faculty " +
			"WHERE b.bookId = :bookId")
	Optional<Book> findBookWithCopiesAndLocation(@Param("bookId") String bookId);

	@Query(value = "SELECT b FROM Book b JOIN FETCH b.publisher p WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))",
			countQuery = "SELECT COUNT(b) FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
	Page<Book> searchByTitleWithPublisher(@Param("title") String title, Pageable pageable);

	@Query("SELECT new com.enigma.library_app.dto.book.BookRatingDto(b.id, b.title, p.name, AVG(br.rating)) " +
			"FROM Book b JOIN b.publisher p JOIN b.reviews br " + // JOIN publisher juga
			"GROUP BY b.id, b.title, p.name " +
			"ORDER BY AVG(br.rating) DESC, COUNT(br) DESC")
	Page<BookRatingDto> findTopRatedBooks(Pageable pageable);

    Book findByIsbn(String number);
}
