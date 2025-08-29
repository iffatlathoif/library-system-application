package com.enigma.library_app.service;

public interface OtpService {

    String buatToken();

    void sendOtpEmail(String toEmail, String otp);
}
