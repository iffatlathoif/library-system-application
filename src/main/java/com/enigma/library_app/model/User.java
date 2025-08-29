package com.enigma.library_app.model;

import com.enigma.library_app.enumeration.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "user_id")
        private String userId;

        @Column(nullable = false, unique = true)
        private String username;

        @Column(name = "full_name")
        private String fullName;

        @Column(name = "password_hash", nullable = false)
        private String passwordHash;

        private String email;

        private String phone;

        @Enumerated(EnumType.STRING)
        private Role roles;

        @OneToOne
        @JoinColumn(name = "member_id",unique = true)
        private Member member;

        @ManyToOne
        @JoinColumn(name = "location_id", nullable = true)
        private Location location;

        private LocalDateTime lastLogin;
        private LocalDateTime createdAt = LocalDateTime.now();

}
