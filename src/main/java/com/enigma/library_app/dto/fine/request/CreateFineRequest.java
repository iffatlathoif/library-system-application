package com.enigma.library_app.dto.fine.request;

import com.enigma.library_app.model.transaction.fine.enumeration.TypeFine;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFineRequest {
	private String loanId;
	private Integer amount; // in IDR
	private TypeFine type;
	private String paymentMethod;
}
