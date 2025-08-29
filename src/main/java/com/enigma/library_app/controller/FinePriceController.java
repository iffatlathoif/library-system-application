package com.enigma.library_app.controller;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.fine.request.CreateFinePriceRequest;
import com.enigma.library_app.dto.fine.response.FinePriceResponse;
import com.enigma.library_app.service.FinePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fine-prices")
public class FinePriceController {
	private final FinePriceService finePriceService;


	@PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
	@PostMapping
	public ResponseEntity<BaseResponse<FinePriceResponse>> create(@RequestBody CreateFinePriceRequest request) {
		FinePriceResponse finePriceResponse = finePriceService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BaseResponse.success(finePriceResponse));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BaseResponse<FinePriceResponse>> getById(@PathVariable("id") Long id) {
		FinePriceResponse finePriceResponse = finePriceService.getById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(finePriceResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<BaseResponse<FinePriceResponse>> updateById(@PathVariable("id") Long id, @RequestBody CreateFinePriceRequest request) {
		FinePriceResponse finePriceResponse = finePriceService.updateById(id, request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(finePriceResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
	@PutMapping("/{id}/activate")
	public ResponseEntity<BaseResponse<FinePriceResponse>> activateFinePrice(@PathVariable Long id) {
		FinePriceResponse activated = finePriceService.activateFinePrice(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(activated));
	}

	@GetMapping("/active")
	public ResponseEntity<BaseResponse<FinePriceResponse>> getActiveFinePrice() {
		FinePriceResponse finePriceResponse = finePriceService.getByActive();
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(finePriceResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<BaseResponse<String>> deleteById(@PathVariable("id") Long id) {
		finePriceService.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success("Fine Price deleted successfully."));
	}
}
