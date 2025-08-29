package com.enigma.library_app.service.impl;

import com.enigma.library_app.model.OtpToken;
import com.enigma.library_app.repository.OtpTokenRepository;
import com.enigma.library_app.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final JavaMailSender mailSender;
    private final OtpTokenRepository otpTokenRepository;

    @Override
    public String buatToken() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Kode OTP Verifikasi Anda");
            message.setText(
                    "Halo,\n\n" +
                            "Gunakan kode ini untuk menyelesaikan proses login Anda. Kode ini hanya berlaku selama 5 menit.\n\n" +
                            "Kode OTP Anda adalah: " + otp + "\n\n" +
                            "Terima kasih,\n" +
                            "Tim Perpustakaan Digital"
            );

            saveOrUpdateOtp(toEmail, otp);

            mailSender.send(message);
            System.out.println("Email OTP berhasil dikirim ke " + toEmail);
        } catch (Exception e) {
            System.err.println("Gagal mengirim email OTP: " + e.getMessage());
        }
    }

    private void saveOrUpdateOtp(String email, String otp) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        OtpToken otpToken = otpTokenRepository.findByEmail(email)
                .orElse(new OtpToken());

        otpToken.setEmail(email);
        otpToken.setOtpCode(otp);
        otpToken.setExpiryTime(expiryTime);

        otpTokenRepository.save(otpToken);
        log.info("OTP untuk email {} berhasil disimpan ke database.", email);
    }
}
