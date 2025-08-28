package com.enigma.library_app.service.contract.book;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.model.master.book.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookReviewService {
    Page<BookReview> getReviewsByBookId(String bookId, Pageable pageable);
    void createOrUpdateReview(String usename, String bookId, Integer rating, String reviewText);
}
