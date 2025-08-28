package com.enigma.library_app.repository;

import com.enigma.library_app.model.master.antrian.AvailabilityNotification;
import com.enigma.library_app.model.master.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityNotificationRepository extends JpaRepository<AvailabilityNotification, String> {
    boolean existsByBookAndIsNotifiedFalse(Book book);
}
