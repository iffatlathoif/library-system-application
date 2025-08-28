package com.enigma.library_app.controller;

import com.enigma.library_app.service.contract.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestParam String email) {
        try {
            String otp = otpService.buatToken();

            otpService.sendOtpEmail(email, otp);

            return ResponseEntity.ok(Map.of("message", "Kode OTP telah berhasil dikirim ke " + email));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Gagal mengirim OTP: " + e.getMessage()));
        }
    }
}