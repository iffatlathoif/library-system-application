package com.enigma.library_app.dto.loan.response;

import com.enigma.library_app.enumeration.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanReportResponse {
    private String loanId;
    private String memberName;
    private String memberEmail;
    private String bookTitle;
    private int quantity;
    private LocalDateTime loanDate;
    private LocalDateTime dueDate;
    private LoanStatus status;
    private String locationName;
}
