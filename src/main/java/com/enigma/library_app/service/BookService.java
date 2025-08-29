package com.enigma.library_app.service;

import com.enigma.library_app.dto.book.BookRatingDto;
import com.enigma.library_app.dto.book.request.CreateBookRequest;
import com.enigma.library_app.dto.book.request.UpdateBookRequest;
import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.copy.request.CreateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.model.Book;
import com.enigma.library_app.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
	Book getEntityById(String id);
	BookResponse create(CreateBookRequest request, Location location);
	BookResponse update(UpdateBookRequest request);
	Page<BookResponse> getAll(Integer page, Integer size);
	BookResponse getById(String id);
	Page<BookResponse> getAllByCategoryId(Long categoryId, Integer page, Integer size);
	List<CopyResponse> addCopiesToBook(Location location, CreateCopyRequest request);
	void deleteById(String id);
	void uploadImage(String id, MultipartFile file);
	Page<Book> findAllBooks(Pageable pageable);
	Page<Book> searchByTitle(String title, Pageable pageable);
	Page<BookRatingDto> findTopRatedBooks(Pageable pageable);
}
