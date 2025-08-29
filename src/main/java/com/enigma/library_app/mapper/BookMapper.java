package com.enigma.library_app.mapper;

import com.enigma.library_app.dto.book.request.CreateBookRequest;
import com.enigma.library_app.dto.book.request.UpdateBookRequest;
import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.model.Book;

import java.util.List;

public class BookMapper {

	public static Book toEntity(CreateBookRequest request) {
		return Book.builder()
				.isbn(request.getIsbn())
				.title(request.getTitle())
				.publicationYear(request.getPublicationYear())
				.language(request.getLanguage())
				.pageCount(request.getPageCount())
				.price(request.getPrice())
				.build();
	}

	public static void updateBookFromRequest(Book book, UpdateBookRequest request) {
		if (request.getIsbn() != null) {
			book.setIsbn(request.getIsbn());
		}

		if (request.getId() != null) {
			book.setTitle(request.getTitle());
		}

		if (request.getPublisherName() != null) {
			book.setPublicationYear(request.getPublicationYear());
		}

		if (request.getLanguage() != null) {
			book.setLanguage(request.getLanguage());
		}

		if (request.getPageCount() != null) {
			book.setPageCount(request.getPageCount());
		}

		if (request.getPrice() != null) {
			book.setPrice(request.getPrice());
		}
	}

	public static BookResponse toDto(Book book) {
		return BookResponse.builder()
				.id(book.getBookId())
				.isbn(book.getIsbn())
				.title(book.getTitle())
				.publisherName(book.getPublisher().getName())
				.categories(CategoryMapper.toDtoList(book))
				.authors(AuthorMapper.toDtoList(book))
				.publicationYear(book.getPublicationYear())
				.language(book.getLanguage())
				.pageCount(book.getPageCount())
				.imageUrl(book.getImageUrl())
				.build();
	}

	public static List<BookResponse> toDtoList(List<Book> books) {
		return books.stream()
				.map(BookMapper::toDto)
				.toList();
	}
}
