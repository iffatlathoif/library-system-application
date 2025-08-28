package com.enigma.library_app.dto.location.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLocationRequest {
	private Long id;
	private String name;
	private String address;
	private String description;
}
