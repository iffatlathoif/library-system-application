package com.enigma.library_app.dto.book.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookRequest {
	private String id;
	private String isbn;
	private String title;
	private String publisherName;
	private List<String> authorNames;
	private List<String> categoryNames;
	private String publicationYear;
	private String language;
	private BigDecimal price;
	private Integer pageCount;
}
