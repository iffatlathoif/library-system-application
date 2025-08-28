package com.enigma.library_app.dto.fine.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFinePriceRequest {
	private BigDecimal price;
	private Boolean isActive;
}
