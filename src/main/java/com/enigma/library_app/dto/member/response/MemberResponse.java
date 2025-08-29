package com.enigma.library_app.dto.member.response;

import com.enigma.library_app.enumeration.Status;
import com.enigma.library_app.enumeration.Type;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
	private String id;
	private String username;
	private String name;
	private String nisNip;
	private String email;
	private String phone;
	private String telegramId; // sekalian masukin telegramId
	private String facultyCode;    // masukin ini juga sekalian
	private Type type;
	private Status status;
	private String photo;

}
