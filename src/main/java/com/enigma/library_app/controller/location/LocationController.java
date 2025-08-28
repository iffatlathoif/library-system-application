package com.enigma.library_app.controller.location;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.PagingResponse;
import com.enigma.library_app.dto.location.request.CreateLocationRequest;
import com.enigma.library_app.dto.location.request.UpdateLocationRequest;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.service.contract.book.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

	private final LocationService locationService;

	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping
	public ResponseEntity<BaseResponse<LocationResponse>> create(@RequestBody CreateLocationRequest request) {
		LocationResponse locationResponse = locationService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BaseResponse.success(locationResponse));
	}

	@GetMapping("/{locationId}")
	public ResponseEntity<BaseResponse<LocationResponse>> getById(@PathVariable("locationId") Long id) {
		LocationResponse locationResponse = locationService.getById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(locationResponse));
	}

	@GetMapping
	public ResponseEntity<BaseResponse<List<LocationResponse>>> getAll(@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
																	   @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
		Page<LocationResponse> pages = locationService.getAll(page, size);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.successWithPaging(pages.getContent(), PagingResponse.builder()
						.currentPage(pages.getNumber())
						.size(pages.getSize())
						.totalPage(pages.getTotalPages())
						.build()));
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@PutMapping("/{locationId}")
	public ResponseEntity<BaseResponse<LocationResponse>> updateById(@PathVariable("locationId") Long id, @RequestBody UpdateLocationRequest request) {
		request.setId(id);
		LocationResponse locationResponse = locationService.updateById(id, request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(locationResponse));
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@DeleteMapping("/{locationId}")
	public ResponseEntity<BaseResponse<String>> deleteById(@PathVariable("locationId") Long id) {
		locationService.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success("Location deleted successfully."));
	}
}
