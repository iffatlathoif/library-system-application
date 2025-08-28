package com.enigma.library_app.dto.book;

import com.enigma.library_app.model.master.book.entity.Book;

public record BookRatingDto(
        String bookId,
        String title,
        String publisherName,
        Double averageRating
) {

}
