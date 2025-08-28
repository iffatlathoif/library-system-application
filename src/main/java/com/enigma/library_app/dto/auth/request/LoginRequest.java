package com.enigma.library_app.dto.auth.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
	private String username;
	private String password;
}
