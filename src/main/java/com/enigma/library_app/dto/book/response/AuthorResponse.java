package com.enigma.library_app.dto.book.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {
	private Long authorId;
	private String name;
}
