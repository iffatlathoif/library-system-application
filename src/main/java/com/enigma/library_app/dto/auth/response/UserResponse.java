package com.enigma.library_app.dto.auth.response;

import com.enigma.library_app.auth.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    //ini untuk pembuatan akun admin & staff
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    // ADMIN, STAFF
    private Role role;
    private Long locationId;
    private String locationName;
}
