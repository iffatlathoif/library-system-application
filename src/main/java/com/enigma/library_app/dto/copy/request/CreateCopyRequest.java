package com.enigma.library_app.dto.copy.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCopyRequest {
	private String bookId;
	private CopyRequest copy;
}
