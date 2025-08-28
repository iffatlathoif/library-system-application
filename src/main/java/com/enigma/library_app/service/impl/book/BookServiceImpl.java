package com.enigma.library_app.service.impl.book;

import com.enigma.library_app.dto.CloudinaryResponse;
import com.enigma.library_app.dto.book.BookRatingDto;
import com.enigma.library_app.dto.book.request.CreateBookRequest;
import com.enigma.library_app.dto.book.request.UpdateBookRequest;
import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.copy.request.CreateCopyRequest;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.exception.ApiException;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.mapper.BookMapper;
import com.enigma.library_app.model.master.book.entity.Author;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.Category;
import com.enigma.library_app.model.master.book.entity.Publisher;
import com.enigma.library_app.model.master.location.entity.Location;
import com.enigma.library_app.repository.BookRepository;
import com.enigma.library_app.service.contract.book.*;
import com.enigma.library_app.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;
	private final AuthorService authorService;
	private final CategoryService categoryService;
	private final PublisherService publisherService;
	private final CopyService copyService;
	private final CloudinaryService cloudinaryService;

	@Transactional
	@Override
	public BookResponse create(CreateBookRequest request, Location location) {
		if (request.getCopy() != null && request.getCopy().getQuantity() <= 0) {
			throw new ApiException("Invalid quantity: must be greater than zero.");
		}
		Set<Author> authors = getAuthorsByName(request.getAuthorNames());
		Set<Category> categories = getCategoriesByName(request.getCategoryNames());
		Publisher publisher = getPublisherByName(request.getPublisherName());

		Book book = BookMapper.toEntity(request);
		book.setImageUrl("https://placehold.co/600x400.png");
		book.setPublisher(publisher);
		book.setAuthor(authors);
		book.setCategory(categories);
		Book savedBook = bookRepository.save(book);

		// generate copies
		copyService.createCopies(savedBook, location, request.getCopy());

		return BookMapper.toDto(savedBook);
	}

	@Transactional
	@Override
	public BookResponse update(UpdateBookRequest request) {
		Book book = getEntityById(request.getId());

		if (!request.getAuthorNames().isEmpty()) {
			Set<Author> authors = getAuthorsByName(request.getAuthorNames());
			book.setAuthor(authors);
		}

		if (!request.getCategoryNames().isEmpty()) {
			Set<Category> categories = getCategoriesByName(request.getCategoryNames());
			book.setCategory(categories);
		}

		if (request.getPublisherName() != null) {
			Publisher publisher = getPublisherByName(request.getPublisherName());
			book.setPublisher(publisher);
		}
		BookMapper.updateBookFromRequest(book, request);

		Book savedBook = bookRepository.save(book);

		return BookMapper.toDto(savedBook);
	}

	@Override
	public Page<BookResponse> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Book> pages = bookRepository.findAll(pageable);
		List<BookResponse> bookDtos = BookMapper.toDtoList(pages.getContent());
		return new PageImpl<>(bookDtos, pageable, pages.getTotalElements());
	}

	@Override
	public BookResponse getById(String id) {
		Book book = getEntityById(id);
		return BookMapper.toDto(book);
	}

	@Override
	public Book getEntityById(String id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
	}

	@Override
	public Page<BookResponse> getAllByCategoryId(Long categoryId, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Category category = categoryService.findById(categoryId);
		Page<Book> pages = bookRepository.findByCategory(category, pageable);
		List<BookResponse> bookDtos = BookMapper.toDtoList(pages.getContent());
		return new PageImpl<>(bookDtos, pageable, pages.getTotalElements());
	}

	@Transactional
	@Override
	public List<CopyResponse> addCopiesToBook(Location location, CreateCopyRequest request) {
		if (request.getCopy() != null && request.getCopy().getQuantity() <= 0) {
			throw new ApiException("Invalid quantity: must be greater than zero.");
		}
		Book book = getEntityById(request.getBookId());
		return copyService.createCopies(book, location, request.getCopy());
	}

	@Transactional
	@Override
	public void deleteById(String id) {
		Book book = getEntityById(id);
		bookRepository.deleteById(book.getBookId());
		if (book.getImagePublicId() != null) {
			this.cloudinaryService.deleteFile(book.getImagePublicId());
		}
	}

	@Transactional
	@Override
	public void uploadImage(String id, MultipartFile file) {
		Book book = getEntityById(id);
		FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PATTERN);
		if (book.getImagePublicId() != null) {
			cloudinaryService.deleteFile(book.getImagePublicId());
		}
		String fileName = FileUploadUtil.getFileName(file.getOriginalFilename());
		CloudinaryResponse response = this.cloudinaryService.uploadFile(file, fileName, "books");
		book.setImageUrl(response.getUrl());
		book.setImagePublicId(response.getPublicId());
		bookRepository.save(book);
	}

	/**
	 * karna tidak ada manajemen publisher / crud ketika create book jika publisher tidak ada di db akan save ke db
	 *
	 * @param name
	 * @return
	 */
	private Publisher getPublisherByName(String name) {
		return publisherService.findByName(name);
	}

	/**
	 * karna tidak ada manajemen author / crud ketika create book jika author tidak ada id akan save author ke db
	 *
	 * @param authorNames
	 * @return
	 */
	private Set<Author> getAuthorsByName(List<String> authorNames) {
		return authorNames.stream()
				.map(authorService::findByName)
				.collect(Collectors.toSet());
	}

	/**
	 * karna tidak ada manajemen category / crud ketika create book jika category tidak ada id akan di save ke db
	 *
	 * @param requests
	 * @return
	 */
	private Set<Category> getCategoriesByName(List<String> requests) {
		return requests.stream().map(categoryService::findByName)
				.collect(Collectors.toSet());
	}

	@Override
	public Page<Book> findAllBooks(Pageable pageable) {
		return bookRepository.findAllWithPublisher(pageable);
	}

	@Override
	public Page<Book> searchByTitle(String title, Pageable pageable) {
		return bookRepository.searchByTitleWithPublisher(title, pageable);
	}

	@Override
	public Page<BookRatingDto> findTopRatedBooks(Pageable pageable) {
		return bookRepository.findTopRatedBooks(pageable);
	}
}
