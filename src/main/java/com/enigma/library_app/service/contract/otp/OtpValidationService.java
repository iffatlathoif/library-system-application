package com.enigma.library_app.service.contract.otp;

public interface OtpValidationService {
    boolean validateOtp(String email, String otp);
}
