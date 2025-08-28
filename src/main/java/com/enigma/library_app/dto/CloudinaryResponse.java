package com.enigma.library_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudinaryResponse {
	private String publicId;
	private String url;
}
