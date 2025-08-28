package com.enigma.library_app.dto.fine.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinePriceResponse {
	private Long id;
	private BigDecimal price;
	private boolean isActive;
}
