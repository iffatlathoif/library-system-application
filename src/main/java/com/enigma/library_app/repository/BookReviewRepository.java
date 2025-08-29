package com.enigma.library_app.repository;

import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.BookReview;
import com.enigma.library_app.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    @Query("SELECT br FROM BookReview br " +
            "JOIN FETCH br.member m " +
            "LEFT JOIN FETCH m.faculty " +
            "WHERE br.book.bookId = :bookId ORDER BY br.createdAt DESC")
    Page<BookReview> findByBookIdWithMember(@Param("bookId") String bookId, Pageable pageable);

    Optional<BookReview> findByBookAndMember(Book book, Member member);
}
