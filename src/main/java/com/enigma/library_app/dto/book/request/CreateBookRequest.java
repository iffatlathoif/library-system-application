package com.enigma.library_app.dto.book.request;

import com.enigma.library_app.dto.copy.request.CopyRequest;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequest {
	private String isbn;
	private String title;
	private String publisherName;
	private List<String> authorNames;
	private List<String> categoryNames;
	private String publicationYear;
	private String language;
	private Integer pageCount;
	private BigDecimal price;
	private CopyRequest copy;
}
