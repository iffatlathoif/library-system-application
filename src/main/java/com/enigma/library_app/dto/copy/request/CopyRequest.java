package com.enigma.library_app.dto.copy.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyRequest {
	private String rackCode;
	private Integer quantity;
}
