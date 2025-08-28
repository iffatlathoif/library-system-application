package com.enigma.library_app.model.master.book.entity;

import com.enigma.library_app.model.master.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "book_reviews", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"book_id", "member_id"})
})
public class BookReview {
	@Id
	@Column(name = "review_id")
	private String id;

	@Column(nullable = false)
	private Integer rating;

	@Column(name = "review_text", columnDefinition = "TEXT")
	private String reviewText;

	@Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
}
