package com.enigma.library_app.dto.copy.request;

import com.enigma.library_app.enumeration.StatusCopies;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCopyRequest {
	private Long locationId;
	private String rackCode;
	private StatusCopies status;
}
