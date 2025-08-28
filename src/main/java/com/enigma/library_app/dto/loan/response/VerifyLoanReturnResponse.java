package com.enigma.library_app.dto.loan.response;

import com.enigma.library_app.dto.member.response.MemberResponse;
import com.enigma.library_app.enumeration.StatusCopies;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyLoanReturnResponse {
	private String loanId;
	private MemberResponse member;
	// private CopyResponse copy;
	private StatusCopies status;
}
