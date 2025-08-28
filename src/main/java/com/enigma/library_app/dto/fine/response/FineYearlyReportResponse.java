package com.enigma.library_app.dto.fine.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FineYearlyReportResponse {
	private Integer year;
	private Integer totalIncome;
	private List<MonthlyFineSummary> monthlyReports;
}
