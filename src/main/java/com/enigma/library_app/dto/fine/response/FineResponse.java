package com.enigma.library_app.dto.fine.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineResponse {
	private String fineId;
	private String loanId;
	private Integer amount;
	private LocalDate issuedDate;
	private LocalDate paidDate;
	private String status;
	private String type;
}
