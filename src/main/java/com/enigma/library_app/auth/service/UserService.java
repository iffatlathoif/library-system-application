package com.enigma.library_app.auth.service;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.dto.auth.request.LoginRequest;
import com.enigma.library_app.dto.auth.request.RegisterRequest;
import com.enigma.library_app.dto.auth.response.LoginResponse;
import com.enigma.library_app.dto.auth.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
Ini service untuk membuat akun Admin & Staff
 */
public interface UserService {
    UserResponse create(RegisterRequest request);
    LoginResponse login(LoginRequest loginRequest);
    String logout(HttpServletRequest request, HttpServletResponse response);
    String delete(String id);
    User getEntityByUsername(String username);
    User getCurrentUser();
    User getId(String id);
}
