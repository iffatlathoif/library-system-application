package com.enigma.library_app.service.impl.book;

import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.BookReview;
import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.repository.BookRepository;
import com.enigma.library_app.repository.BookReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enigma.library_app.repository.UserRepository; // <-- 1. Tambahkan import ini
import static org.mockito.ArgumentMatchers.any; // <-- Pastikan import ini ada

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class BookReviewServiceImplTest {

    @Mock
    private BookReviewRepository bookReviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository; // <-- 2. Tambahkan Mock untuk UserRepository

    @InjectMocks
    private BookReviewServiceImpl bookReviewService;

    // ... test getReviewsByBookId tidak berubah ...

    @Test
    void whenCreateNewReview_shouldSaveNewReview() {
        // Arrange
        String username = "farid"; // Gunakan username
        User user = new User();
        Member member = new Member();
        user.setMember(member);
        String bookId = "book-1";
        Book book = new Book();
        book.setBookId(bookId);

        // 3. Mock behavior untuk userRepository
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookReviewRepository.findByBookAndMember(book, member)).thenReturn(Optional.empty());

        // 4. Act: Panggil metode baru dengan username
        bookReviewService.createOrUpdateReview(username, bookId, 5, "Great book!");

        // Assert (bagian ini tetap sama)
        ArgumentCaptor<BookReview> reviewCaptor = ArgumentCaptor.forClass(BookReview.class);
        verify(bookReviewRepository).save(reviewCaptor.capture());

        BookReview savedReview = reviewCaptor.getValue();
        assertNotNull(savedReview.getId());
        assertEquals(book, savedReview.getBook());
        assertEquals(member, savedReview.getMember());
        assertEquals(5, savedReview.getRating());
        assertEquals("Great book!", savedReview.getReviewText());
    }

    @Test
    void whenUpdateExistingReview_shouldUpdateAndSaveChanges() {
        // Arrange
        String username = "farid"; // Gunakan username
        User user = new User();
        Member member = new Member();
        user.setMember(member);
        String bookId = "book-1";
        Book book = new Book();
        book.setBookId(bookId);
        BookReview existingReview = new BookReview();
        existingReview.setId("review-1");
        existingReview.setRating(3);

        // Mock behavior untuk userRepository
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookReviewRepository.findByBookAndMember(book, member)).thenReturn(Optional.of(existingReview));

        // Act: Panggil metode baru dengan username
        bookReviewService.createOrUpdateReview(username, bookId, 5, "Updated review");

        // Assert (bagian ini tetap sama)
        ArgumentCaptor<BookReview> reviewCaptor = ArgumentCaptor.forClass(BookReview.class);
        verify(bookReviewRepository).save(reviewCaptor.capture());

        BookReview savedReview = reviewCaptor.getValue();
        assertEquals("review-1", savedReview.getId());
        assertEquals(5, savedReview.getRating());
        assertEquals("Updated review", savedReview.getReviewText());
    }

    @Test
    void whenCreateReviewForNonExistentBook_shouldThrowNotFoundException() {
        // Arrange
        String username = "farid";
        User user = new User();

        // Cukup mock userRepository, karena pengecekan buku terjadi setelahnya
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(bookRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> bookReviewService.createOrUpdateReview(username, "non-existent-book", 5, ""));
    }
}
