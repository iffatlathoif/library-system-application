package com.enigma.library_app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telegram_users")
public class TelegramUser {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "telegram_id")
	private String telegramId;

	@Column(name = "chat_id", unique = true)
	private Long chatId;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "member_id", referencedColumnName = "member_id", unique = true)
	private Member member;

	public TelegramUser(Long chatId, Member member) {
		this.chatId = chatId;
		this.member = member;
	}
}