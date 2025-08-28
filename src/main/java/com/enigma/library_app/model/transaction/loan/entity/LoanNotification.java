package com.enigma.library_app.model.transaction.loan.entity;

import com.enigma.library_app.model.master.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "loan_notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate notificationDate;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean isSent = false;
}
