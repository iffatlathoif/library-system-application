package com.enigma.library_app.repository;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.BookSubscription;
import com.enigma.library_app.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookSubscriptionRepository extends JpaRepository<BookSubscription, String> {

    // Untuk memeriksa apakah user sudah subscribe buku ini
    boolean existsByMemberAndBook(Member member, Book book);

    @Query("SELECT sub FROM BookSubscription sub JOIN FETCH sub.book WHERE sub.member = :member AND sub.isNotified = false")
    List<BookSubscription> findByMemberAndIsNotifiedFalse(Member member);

    // Untuk mendapatkan semua member yang menunggu notifikasi sebuah buku
    List<BookSubscription> findByBookAndIsNotifiedFalse(Book book);
}
