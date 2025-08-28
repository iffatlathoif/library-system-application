package com.enigma.library_app.dto.loan.request;

import com.enigma.library_app.enumeration.StatusCopies;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyLoanReturnRequest {
	private String memberId;
	private String copyId;
	private StatusCopies status;
}
