package com.enigma.library_app.model;

import com.enigma.library_app.enumeration.CardPrintStatus;
import com.enigma.library_app.enumeration.Status;
import com.enigma.library_app.enumeration.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.*;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Members")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "member_id", length = 36)
	private String memberId;

	@Column(nullable = false, unique = true)
	private String nisNip;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false, unique = true)
	private String phone;
	private String photo;


	@ManyToOne
	@JoinColumn(name = "faculty_code", referencedColumnName = "faculty_code")
	private Faculty faculty;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Type type;

	private LocalDate joinDate;
	@Enumerated(EnumType.STRING)
	private Status status;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "card_print_status", nullable = false)
	private CardPrintStatus cardPrintStatus;

	@OneToMany(mappedBy = "member")
	private List<Loan> loans;

	@OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
	private TelegramUser telegramUser;
}
