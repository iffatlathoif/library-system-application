package com.enigma.library_app.advice;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.exception.ApiException;
import com.enigma.library_app.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<BaseResponse<String>> handleResourceNotFoundException(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(BaseResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<BaseResponse<String>> handleApiException(ApiException exception) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(BaseResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse<String>> handleException(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(BaseResponse.error(exception.getMessage()));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<BaseResponse<String>> handleResponseStatus(ResponseStatusException ex) {
		return ResponseEntity
				.status(ex.getStatusCode()) // 404, 400, dst.
				.body(BaseResponse.error(ex.getReason()));
	}

	@ExceptionHandler(AuthorizationDeniedException.class)
	public ResponseEntity<BaseResponse<String>> handleAccessDenied(AuthorizationDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(BaseResponse.error("Access Denied"));
	}
}
