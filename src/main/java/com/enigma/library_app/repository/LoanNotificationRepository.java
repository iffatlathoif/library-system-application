package com.enigma.library_app.repository;

import com.enigma.library_app.model.Member;
import com.enigma.library_app.model.LoanNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LoanNotificationRepository extends JpaRepository<LoanNotification, Long> {
    boolean existsByMemberAndNotificationDate(Member member, LocalDate notificationDate);
}
