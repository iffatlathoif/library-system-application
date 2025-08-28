package com.enigma.library_app.dto.copy.response;

import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.enumeration.StatusCopies;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyResponse {
	private Long copyId;
	private BookResponse book;
	private LocationResponse location;
	private String rackCode;
	private StatusCopies status;
}
