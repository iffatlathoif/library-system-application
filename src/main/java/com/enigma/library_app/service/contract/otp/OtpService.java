package com.enigma.library_app.service.contract.otp;

public interface OtpService {

    String buatToken();

    void sendOtpEmail(String toEmail, String otp);
}
