package com.enigma.library_app.controller;

import com.enigma.library_app.service.UserService;
import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.auth.request.LoginRequest;
import com.enigma.library_app.dto.auth.request.RegisterRequest;
import com.enigma.library_app.dto.auth.response.LoginResponse;
import com.enigma.library_app.dto.auth.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/*
Yang bisa membuat dan menghapus staff cuma admin
 */
@RestController
public class UserController {
    @Value("${app.admin.secret-path}")
    private String adminSecretPath;

    @Value("${admin.key}")
    private String adminKey;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(
            path = "/api/auth/register/staff",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<UserResponse> createStaff(@RequestBody RegisterRequest request){
        UserResponse userResponse = userService.create(request);
        return BaseResponse.<UserResponse>builder().data(userResponse).build();
    }
    @PostMapping(
            path = "${app.admin.secret-path}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<UserResponse> createAdmin(
            @RequestHeader(value = "X-ADMIN-KEY", required = true) String key,
            @RequestBody RegisterRequest request){
        if (!adminKey.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kunci rahasia salah!");
        }
        UserResponse userResponse = userService.create(request);
        return BaseResponse.<UserResponse>builder().data(userResponse).build();
    }

    @PostMapping(
            path = "/api/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest){
        LoginResponse loginResponse = userService.login(loginRequest);
        return BaseResponse.success(loginResponse);
    }

    @PreAuthorize("hasAuthority('STAFF')")
    @GetMapping(
            path = "/api/auth/test-token"
    )
    public BaseResponse<String> testToken(@RequestHeader("Authorization") String token){
        return BaseResponse.success(token);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping(
            path = "/api/staff/{staffId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<String> deleteStaff(@PathVariable("staffId") String staffId){
        String kode = userService.delete(staffId);
        return BaseResponse.<String>builder()
                .data("Staff "+ kode + " Telah Dihapus!").build();
    }
    @DeleteMapping(
            path = "/api/admin/{adminId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<String> deleteAdmin(@PathVariable("adminId") String adminId){
        String kode = userService.delete(adminId);
        return BaseResponse.<String>builder()
                .data("Admin "+ kode + " Telah Dihapus!").build();
    }

    @PostMapping("/api/auth/logout")
    public BaseResponse<String> logout(HttpServletRequest request, HttpServletResponse response){
        String logout = userService.logout(request, response);
        return BaseResponse.success(logout);
    }
}
