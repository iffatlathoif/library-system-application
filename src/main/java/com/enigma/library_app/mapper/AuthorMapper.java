package com.enigma.library_app.mapper;

import com.enigma.library_app.dto.book.response.AuthorResponse;
import com.enigma.library_app.model.Author;
import com.enigma.library_app.model.Book;

import java.util.List;

public class AuthorMapper {
	public static AuthorResponse toDto(Author author) {
		return AuthorResponse.builder()
				.authorId(author.getAuthorId())
				.name(author.getName())
				.build();
	}

	public static List<AuthorResponse> toDtoList(Book book) {
		return book.getAuthor().stream()
				.map(AuthorMapper::toDto)
				.toList();
	}
}
