package com.enigma.library_app.controller;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.copy.request.UpdateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.service.CopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/copies")
@RequiredArgsConstructor
public class CopyController {

	private final CopyService copyService;

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@GetMapping("/{copyId}")
	public ResponseEntity<BaseResponse<CopyResponse>> getById(@PathVariable(name = "copyId") Long id) {
		CopyResponse copyResponse = copyService.getById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(copyResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@PutMapping("/{copyId}")
	public ResponseEntity<BaseResponse<CopyResponse>> updateById(@PathVariable("copyId") Long id, @RequestBody UpdateCopyRequest request) {
		CopyResponse copyResponse = copyService.updateById(id, request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(copyResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@DeleteMapping("/{copyId}")
	public ResponseEntity<BaseResponse<String>> deleteById(@PathVariable("copyId") Long id) {
		copyService.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success("Copy deleted successfully."));
	}
}
