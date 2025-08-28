package com.enigma.library_app.service.impl.auth;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.repository.UserRepository;
import com.enigma.library_app.service.contract.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> loginMemberViaBot(String username, String password) {
        log.info("Mencoba login untuk username: {}", username);

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            log.info("User '{}' ditemukan di database.", username);
            User user = userOpt.get();
            String storedHash = user.getPasswordHash();
            log.info("Hash dari DB: {}", storedHash);
            log.info("Password input: {}", password);

            boolean isPasswordMatch = passwordEncoder.matches(password, storedHash);
            log.info("Hasil pengecekan password: {}", isPasswordMatch);

            if (isPasswordMatch) {
                log.info("Login berhasil untuk user '{}'", username);
                return Optional.of(user);
            } else {
                log.error("Login gagal: Password tidak cocok untuk user '{}'", username);
            }
        } else {
            log.error("Login gagal: User '{}' tidak ditemukan di database.", username);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        if (newPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password minimal harus 6 karakter.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password untuk user {} berhasil diubah.", username);
    }
}
