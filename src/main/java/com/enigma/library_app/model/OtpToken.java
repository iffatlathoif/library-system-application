package com.enigma.library_app.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @Column(name = "email", nullable = false, unique = true)
    private String email; // Menggunakan email sebagai ID unik

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

}
