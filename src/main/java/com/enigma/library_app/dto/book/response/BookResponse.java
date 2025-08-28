package com.enigma.library_app.dto.book.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {
	private String id;
	private String isbn;
	private String title;
	private String imageUrl;
	private String publisherName;
	private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
	private String publicationYear;
	private String language;
	private Integer pageCount;

    public BookResponse(String bookId, String title) {
		this.id = bookId;
		this.title = title;
    }
}
