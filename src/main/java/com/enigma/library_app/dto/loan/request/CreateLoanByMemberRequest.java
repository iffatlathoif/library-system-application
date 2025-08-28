package com.enigma.library_app.dto.loan.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateLoanByMemberRequest {
    @NotBlank
    private String bookId;
    @NotNull
    private Long   copyId;
}
