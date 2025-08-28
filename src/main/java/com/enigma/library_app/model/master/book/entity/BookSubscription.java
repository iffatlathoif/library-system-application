package com.enigma.library_app.model.master.book.entity;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "book_subscriptions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "book_id"})
})
public class BookSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "is_notified", nullable = false)
    private boolean isNotified = false;
}
