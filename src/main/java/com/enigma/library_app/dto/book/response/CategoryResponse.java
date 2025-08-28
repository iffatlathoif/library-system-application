package com.enigma.library_app.dto.book.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
	private Long categoryId;
	private String name;
}
