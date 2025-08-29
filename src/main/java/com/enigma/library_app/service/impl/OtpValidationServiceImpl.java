package com.enigma.library_app.service.impl;

import com.enigma.library_app.model.OtpToken;
import com.enigma.library_app.repository.OtpTokenRepository;
import com.enigma.library_app.service.OtpValidationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpValidationServiceImpl implements OtpValidationService {

    private final OtpTokenRepository otpTokenRepository;

    @Override
    @Transactional
    public boolean validateOtp(String email, String otp) {
        Optional<OtpToken> otpTokenOptional = otpTokenRepository.findByEmail(email);

        if (otpTokenOptional.isEmpty()) {
            return false;
        }

        OtpToken otpToken = otpTokenOptional.get();

        if (otpToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpTokenRepository.delete(otpToken);
            return false;
        }

        if (!otpToken.getOtpCode().equals(otp)) {
            return false;
        }

        otpTokenRepository.delete(otpToken);

        return true;
    }
}
