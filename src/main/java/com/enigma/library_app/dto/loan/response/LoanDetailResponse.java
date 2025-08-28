package com.enigma.library_app.dto.loan.response;

import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDetailResponse {
    private String bookId;
    private String title;
    private int quantity;

    private List<CopyResponse> copies;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CopyInLoanResponse {
        private Long copyId;
        private String code;
    }


}
