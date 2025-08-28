package com.enigma.library_app.dto.member.request;

import com.enigma.library_app.auth.constant.Role;
import com.enigma.library_app.model.master.member.enumeration.Status;
import com.enigma.library_app.model.master.member.enumeration.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberRequest {
    @JsonIgnore
    private String id;
    @NotBlank
    @Size(max = 20)
    private String username;
    @NotBlank
    @Size(max = 100)
    private String password;
    @NotBlank
    @Size(max = 100)
    private String name;
    @NotBlank
    @Size(max = 20)
    private String nisNip;
    @NotBlank
    @Size(max = 100)
    private String email;
    @NotBlank
    @Size(max = 20)
    private String phone;
    private String telegramId; // sekalian masukin telegramId
    @NotBlank
    private String facultyCode;    // masukin ini juga sekalian
    @NotNull
    private Role role;
    @NotNull
    private Type type;
    @NotNull
    private Status status; //status (ACTIVE,INACTIVE, BLOCKED)
}
