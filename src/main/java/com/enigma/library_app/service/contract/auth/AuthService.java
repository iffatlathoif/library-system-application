package com.enigma.library_app.service.contract.auth;

import com.enigma.library_app.auth.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface AuthService {
    Optional<User> loginMemberViaBot(String username, String password);
    void changePassword(String username, String newPassword);

}
