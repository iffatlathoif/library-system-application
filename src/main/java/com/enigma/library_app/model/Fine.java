package com.enigma.library_app.model;

import com.enigma.library_app.enumeration.PaymentMethod;
import com.enigma.library_app.enumeration.PaymentStatus;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "fine_id")
    private String fineId;

    @Column(name = "loan_id")
    private String loanId;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @Column(nullable = false)
    private Integer amount;

    private LocalDate issuedDate = LocalDate.now();
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // PENDING, PAID, FAILED

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // TUNAI, TRANSFER

    @Column(name = "payment_url")
    private String paymentUrl;


}
