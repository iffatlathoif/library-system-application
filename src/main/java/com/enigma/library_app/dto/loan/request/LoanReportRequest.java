package com.enigma.library_app.dto.loan.request;

import com.enigma.library_app.model.transaction.loan.constant.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private LoanStatus status; // optional
    private int page = 0;
    private int size = 100;
}
