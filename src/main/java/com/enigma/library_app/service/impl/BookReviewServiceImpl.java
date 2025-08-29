package com.enigma.library_app.service.impl;

import com.enigma.library_app.model.User;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.BookReview;
import com.enigma.library_app.model.Member;
import com.enigma.library_app.repository.BookRepository;
import com.enigma.library_app.repository.BookReviewRepository;
import com.enigma.library_app.repository.UserRepository;
import com.enigma.library_app.service.BookReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final BookRepository bookRespository;
    private final UserRepository userRepository;

    @Override
    public Page<BookReview> getReviewsByBookId(String bookId, Pageable pageable) {
        return bookReviewRepository.findByBookIdWithMember(bookId, pageable);
    }

    @Override
    @Transactional
    public void createOrUpdateReview(String username, String bookId, Integer rating, String reviewText) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        Book book = bookRespository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buku tidak ditemukan"));

        Member member = user.getMember();

        BookReview review = bookReviewRepository.findByBookAndMember(book, member)
                .orElse(new BookReview());

        if (review.getId() == null) {
            review.setId(UUID.randomUUID().toString());
            review.setBook(book);
            review.setMember(member);
        }

        review.setRating(rating);
        review.setReviewText(reviewText);
        review.setCreatedAt(LocalDateTime.now());

        bookReviewRepository.save(review);
    }
}
