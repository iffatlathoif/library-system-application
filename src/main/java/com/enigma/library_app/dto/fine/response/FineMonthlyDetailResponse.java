package com.enigma.library_app.dto.fine.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineMonthlyDetailResponse {
	private Integer year;
	private String month;
	private Integer totalPaid;
	private Integer transactions;
	private List<FineDetailResponse> fines;
}
