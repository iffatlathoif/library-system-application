package com.enigma.library_app.dto.loan.response;

import com.enigma.library_app.enumeration.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponse {
    private String loanId;
    private String memberId;
    private String memberName;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LoanStatus status;
    private List<LoanDetailResponse> details;
}
