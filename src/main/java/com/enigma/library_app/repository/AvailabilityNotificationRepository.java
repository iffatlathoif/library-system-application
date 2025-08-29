package com.enigma.library_app.repository;

import com.enigma.library_app.model.AvailabilityNotification;
import com.enigma.library_app.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityNotificationRepository extends JpaRepository<AvailabilityNotification, String> {
    boolean existsByBookAndIsNotifiedFalse(Book book);
}
