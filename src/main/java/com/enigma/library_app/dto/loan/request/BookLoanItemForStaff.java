package com.enigma.library_app.dto.loan.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookLoanItemForStaff {
    private String bookId;
    private int quantity;
}
