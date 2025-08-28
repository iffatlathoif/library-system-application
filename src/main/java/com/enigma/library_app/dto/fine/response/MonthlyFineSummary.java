package com.enigma.library_app.dto.fine.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyFineSummary {
	private String month;
	private Integer totalPaid;
	private Integer transactions;
}
