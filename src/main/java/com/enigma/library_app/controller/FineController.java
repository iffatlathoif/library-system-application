package com.enigma.library_app.controller;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.fine.response.FineMonthlyDetailResponse;
import com.enigma.library_app.dto.fine.response.FineResponse;
import com.enigma.library_app.dto.fine.response.FineYearlyReportResponse;
import com.enigma.library_app.service.FineService;
import com.enigma.library_app.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FineController {

	private final FineService fineService;

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@GetMapping("/reports/fines")
	public ResponseEntity<BaseResponse<?>> findAllRevenue(@RequestParam(value = "month", required = false) Integer month, @RequestParam(value = "year", required = false) Integer year) {
		if (month != null) {
			FineMonthlyDetailResponse monthlyDetail = fineService.findMonthlyDetail(month, year);
			return ResponseEntity.status(HttpStatus.OK)
					.body(BaseResponse.builder()
							.data(monthlyDetail)
							.build());
		}
		FineYearlyReportResponse monthlySummaryYear = fineService.findMonthlySummaryYear(month, year);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.builder()
						.data(monthlySummaryYear)
						.build());
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@GetMapping("/fines")
	public ResponseEntity<BaseResponse<List<FineResponse>>> getAllByDateRange(@RequestParam(value = "startDate", required = false) String startDate,
																			  @RequestParam(value = "endDate", required = false) String endDate) {
		LocalDate startLocal = DateUtil.formatDate(startDate);
		LocalDate endLocal = DateUtil.formatDate(endDate);
		List<FineResponse> fineResponseDtos = fineService.findByDateRange(startLocal, endLocal);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(fineResponseDtos));
	}
}
