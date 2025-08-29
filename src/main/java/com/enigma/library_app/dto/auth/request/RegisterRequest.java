package com.enigma.library_app.dto.auth.request;

import com.enigma.library_app.enumeration.Role;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
	//ini untuk pembuatan akun admin & staff
	private String username;
	private String password;
	private String fullName;
	private String email;
	private String phone;
	// ADMIN, STAFF
	private Role role;
	@Column(nullable = true)
	private Long locationId;
}
