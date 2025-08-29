package com.enigma.library_app.service;

import com.enigma.library_app.model.User;

import java.util.Optional;

public interface AuthService {
    Optional<User> loginMemberViaBot(String username, String password);
    void changePassword(String username, String newPassword);

}
