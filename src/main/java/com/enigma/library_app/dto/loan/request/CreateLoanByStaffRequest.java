package com.enigma.library_app.dto.loan.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanByStaffRequest {
    private String memberId;
    private List<BookLoanItemForStaff> items;
}
