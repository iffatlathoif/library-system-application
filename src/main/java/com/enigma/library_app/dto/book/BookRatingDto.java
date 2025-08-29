package com.enigma.library_app.dto.book;

public record BookRatingDto(
        String bookId,
        String title,
        String publisherName,
        Double averageRating
) {

}
