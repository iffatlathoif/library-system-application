package com.enigma.library_app.service.impl.otp;

import com.enigma.library_app.model.OtpToken;
import com.enigma.library_app.repository.OtpTokenRepository;
import com.enigma.library_app.service.impl.OtpValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class OtpValidationServiceImplTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @InjectMocks
    private OtpValidationServiceImpl otpValidationService;

    @Test
    void whenValidateOtp_withValidToken_shouldReturnTrueAndDeleteToken() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        OtpToken token = new OtpToken();
        token.setOtpCode(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpTokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        // Act
        boolean isValid = otpValidationService.validateOtp(email, otp);

        // Assert
        assertTrue(isValid);
        verify(otpTokenRepository, times(1)).delete(token);
    }

    @Test
    void whenValidateOtp_withInvalidToken_shouldReturnFalse() {
        // Arrange
        String email = "test@example.com";
        String correctOtp = "123456";
        String wrongOtp = "654321";
        OtpToken token = new OtpToken();
        token.setOtpCode(correctOtp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        when(otpTokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        // Act
        boolean isValid = otpValidationService.validateOtp(email, wrongOtp);

        // Assert
        assertFalse(isValid);
        verify(otpTokenRepository, never()).delete(any());
    }

    @Test
    void whenValidateOtp_withExpiredToken_shouldReturnFalseAndDeleteToken() {
        // Arrange
        String email = "test@example.com";
        String otp = "123456";
        OtpToken token = new OtpToken();
        token.setOtpCode(otp);
        token.setExpiryTime(LocalDateTime.now().minusMinutes(1)); // Expired

        when(otpTokenRepository.findByEmail(email)).thenReturn(Optional.of(token));

        // Act
        boolean isValid = otpValidationService.validateOtp(email, otp);

        // Assert
        assertFalse(isValid);
        verify(otpTokenRepository, times(1)).delete(token);
    }

    @Test
    void whenValidateOtp_withNoTokenFound_shouldReturnFalse() {
        // Arrange
        String email = "nonexistent@example.com";
        when(otpTokenRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean isValid = otpValidationService.validateOtp(email, "any-otp");

        // Assert
        assertFalse(isValid);
        verify(otpTokenRepository, never()).delete(any());
    }
}
