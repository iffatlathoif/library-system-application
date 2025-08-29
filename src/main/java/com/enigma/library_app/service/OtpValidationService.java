package com.enigma.library_app.service;

public interface OtpValidationService {
    boolean validateOtp(String email, String otp);
}
