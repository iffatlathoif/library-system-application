package com.enigma.library_app.controller;

import com.enigma.library_app.model.User;
import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.PagingResponse;
import com.enigma.library_app.dto.book.request.CreateBookRequest;
import com.enigma.library_app.dto.book.request.UpdateBookRequest;
import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.copy.request.CreateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.service.BookService;
import com.enigma.library_app.service.CopyService;
import com.enigma.library_app.util.UserLoggedIn;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;
	private final CopyService copyService;
	private final UserLoggedIn userLoggedIn;

	@PreAuthorize("hasAuthority('STAFF')")
	@PostMapping
	public ResponseEntity<BaseResponse<BookResponse>> create(@RequestBody CreateBookRequest request) {
		User user = userLoggedIn.getUserLoggedIn();
		BookResponse bookResponse = bookService.create(request, user.getLocation());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BaseResponse.success(bookResponse));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','MEMBER')")
	@GetMapping
	public ResponseEntity<BaseResponse<List<BookResponse>>> getAll(@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
																   @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
		Page<BookResponse> pages = bookService.getAll(page, size);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.successWithPaging(pages.getContent(),
						PagingResponse.builder()
								.currentPage(pages.getNumber())
								.size(pages.getSize())
								.totalPage(pages.getTotalPages())
								.build()));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','MEMBER')")
	@GetMapping("/{bookId}")
	public ResponseEntity<BaseResponse<BookResponse>> getById(@PathVariable("bookId") String id) {
		BookResponse bookDto = bookService.getById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(bookDto));
	}

	@PreAuthorize("hasAnyAuthority('STAFF','MEMBER')")
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<BaseResponse<List<BookResponse>>> getByCategoryId(@PathVariable("categoryId") Long categoryId,
																			@RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
																			@RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
		Page<BookResponse> pages = bookService.getAllByCategoryId(categoryId, page, size);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.successWithPaging(pages.getContent(),
						PagingResponse.builder()
								.currentPage(pages.getNumber())
								.size(pages.getSize())
								.totalPage(pages.getTotalPages())
								.build()));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@PutMapping("/{bookId}")
	public ResponseEntity<BaseResponse<BookResponse>> update(@PathVariable("bookId") String id, @RequestBody UpdateBookRequest request) {
		request.setId(id);
		BookResponse bookUpdateDto = bookService.update(request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(bookUpdateDto));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@DeleteMapping("/{bookId}")
	public ResponseEntity<BaseResponse<String>> deleteById(@PathVariable("bookId") String id) {
		bookService.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success("Book deleted successfully"));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@PostMapping("/{bookId}/cover")
	public ResponseEntity<BaseResponse<String>> uploadImage(@PathVariable("bookId") String id, @RequestPart("file") MultipartFile file) {
		bookService.uploadImage(id, file);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success("Upload book image successfully."));
	}

	@PreAuthorize("hasAnyAuthority('MEMBER')")
	@GetMapping("/availability")
	public ResponseEntity<BaseResponse<List<CopyResponse>>> getAvailableByLocation(@RequestParam(value = "location", required = false) String locationName, @RequestParam(value = "faculty", required = false) String facultyName) {
		List<CopyResponse> availabilityBook = copyService.getAvailabilityLocationFaculty(locationName, facultyName);
		return ResponseEntity.status(HttpStatus.OK)
				.body(BaseResponse.success(availabilityBook));
	}

	@PreAuthorize("hasAnyAuthority('STAFF')")
	@PostMapping("/{bookId}/copies")
	public ResponseEntity<BaseResponse<List<CopyResponse>>> addCopiesToBook(@PathVariable("bookId") String id, @RequestBody CreateCopyRequest request) {
		User user = userLoggedIn.getUserLoggedIn();
		request.setBookId(id);
		List<CopyResponse> copyResponses = bookService.addCopiesToBook(user.getLocation(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(BaseResponse.success(copyResponses));
	}
}
