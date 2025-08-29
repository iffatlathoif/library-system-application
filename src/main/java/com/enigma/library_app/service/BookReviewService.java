package com.enigma.library_app.service;

import com.enigma.library_app.model.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewService {
    Page<BookReview> getReviewsByBookId(String bookId, Pageable pageable);
    void createOrUpdateReview(String usename, String bookId, Integer rating, String reviewText);
}
