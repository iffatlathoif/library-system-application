package com.enigma.library_app.model;

import com.enigma.library_app.enumeration.StatusCopies;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "copies")
public class Copy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "copy_id")
	private Long copyId;

	@ManyToOne
	@JoinColumn(name = "book_id", nullable = false, columnDefinition = "varchar(255)")
	private Book book;

	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;

	private String rackCode;

	@Enumerated(EnumType.STRING)
	private StatusCopies status;
	private LocalDate acquisitionDate = LocalDate.now();

	@ManyToOne
	@JoinColumn(name = "loan_detail_id")
	private LoanDetail loanDetail;
}