package com.enigma.library_app.dto.loan.request;

import com.enigma.library_app.enumeration.LoanStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLoanRequest {
    @NotEmpty(message = "Items tidak boleh kosong")
    private List<BookLoanItemForStaff> items;
    private LoanStatus status;
}
