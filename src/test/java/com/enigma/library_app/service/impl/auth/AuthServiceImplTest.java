package com.enigma.library_app.service.impl.auth;

import com.enigma.library_app.model.User;
import com.enigma.library_app.repository.UserRepository;
import com.enigma.library_app.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void whenLoginWithCorrectCredentials_shouldReturnUser() {
        String username = "testuser";
        String password = "password123";
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

        // Act
        Optional<User> result = authService.loginMemberViaBot(username, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void whenLoginWithWrongPassword_shouldReturnEmpty() {
        // Arrange
        String username = "testuser";
        String password = "wrongPassword";
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(false);

        // Act
        Optional<User> result = authService.loginMemberViaBot(username, password);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void whenLoginWithNonExistentUser_shouldReturnEmpty() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.loginMemberViaBot(username, "anyPassword");

        // Assert
        assertFalse(result.isPresent());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void whenChangePassword_withValidData_shouldSucceed() {
        // Arrange
        String username = "testuser";
        String newPassword = "newPassword123";
        String newHashedPassword = "newHashedPassword";
        User user = new User();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(newHashedPassword);

        // Act
        authService.changePassword(username, newPassword);

        // Assert & Verify
        verify(userRepository, times(1)).save(user);
        assertEquals(newHashedPassword, user.getPasswordHash());
    }

    @Test
    void whenChangePassword_forNonExistentUser_shouldThrowNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.changePassword(username, "anyPassword"));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void whenChangePassword_withShortPassword_shouldThrowBadRequestException() {
        // Arrange
        String username = "testuser";
        String shortPassword = "123";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.changePassword(username, shortPassword));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
