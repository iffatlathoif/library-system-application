package com.enigma.library_app.dto.fine.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FineDetailResponse {
	private String fineId;
	private String loanId;
	private Integer amount;
	private LocalDate issuedDate;
	private LocalDate paidDate;
	private String status;
}
