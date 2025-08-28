package com.enigma.library_app.service.impl.loan;
import com.enigma.library_app.auth.constant.Role;
import com.enigma.library_app.auth.service.NotFoundException;
import com.enigma.library_app.model.transaction.fine.entity.FinePrice;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.enigma.library_app.auth.entity.User;
import com.enigma.library_app.auth.service.UserService;
import com.enigma.library_app.common.ValidationService;
import com.enigma.library_app.dto.book.response.AuthorResponse;
import com.enigma.library_app.dto.book.response.BookResponse;
import com.enigma.library_app.dto.book.response.CategoryResponse;
import com.enigma.library_app.dto.copy.response.CopyResponse;
import com.enigma.library_app.dto.loan.request.*;
import com.enigma.library_app.dto.loan.response.LoanDetailResponse;
import com.enigma.library_app.dto.loan.response.LoanReportResponse;
import com.enigma.library_app.dto.loan.response.LoanResponse;
import com.enigma.library_app.dto.location.response.LocationResponse;
import com.enigma.library_app.enumeration.StatusCopies;
import com.enigma.library_app.mapper.BookMapper;
import com.enigma.library_app.mapper.LocationMapper;
import com.enigma.library_app.model.master.book.entity.Author;
import com.enigma.library_app.handlers.LibraryBot;
import com.enigma.library_app.exception.ResourceNotFoundException;
import com.enigma.library_app.model.master.book.entity.Book;
import com.enigma.library_app.model.master.book.entity.Category;
import com.enigma.library_app.model.master.book.entity.Copy;
import com.enigma.library_app.model.master.location.entity.Location;
import com.enigma.library_app.model.master.member.entity.Member;
import com.enigma.library_app.model.transaction.loan.constant.LoanStatus;
import com.enigma.library_app.model.transaction.loan.entity.Loan;
import com.enigma.library_app.model.transaction.loan.entity.LoanDetail;
import com.enigma.library_app.repository.AvailabilityNotificationRepository;
import com.enigma.library_app.repository.*;
import com.enigma.library_app.service.contract.wishlist.SubscriptionService;
import com.enigma.library_app.service.contract.loan.LoanService;
import com.enigma.library_app.service.contract.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LoanServiceImpl implements LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanDetailRepository loanDetailRepository;

    @Autowired
    private CopyRepository copyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AvailabilityNotificationRepository notificationRepository;

    @Value("${library.loan.duration-days}")
    private int loanDurationInDays;

    @Autowired
    @Lazy
    private LibraryBot libraryBot;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private FinePriceRepository finePriceRepository;

    @Transactional
    @Override
    public LoanResponse createLoanByStaff(CreateLoanByStaffRequest request) {
        // Validasi request pakai javax / custom validator
        validationService.validate(request);

        // Ambil user login dan lokasi user
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Location userLocation = currentUser.getLocation();

        // Ambil member peminjam
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member tidak ditemukan"));

        // Validasi stok setiap buku
        for (BookLoanItemForStaff item : request.getItems()) {
            long available = copyRepository.countByBook_BookIdAndLocation_LocationIdAndStatus(
                    item.getBookId(), userLocation.getLocationId(), StatusCopies.AVAILABLE);
            if (item.getQuantity() > available) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Stok buku %s cuma %d, kamu minta %d", item.getBookId(), available, item.getQuantity()));
            }
        }

        // Inisialisasi loan
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(7));
        loan.setStatus(LoanStatus.ONGOING);
        loan.setLoanDetails(new ArrayList<>()); // WAJIB

        // Ambil semua book yang diminta
        List<String> bookIds = request.getItems().stream()
                .map(BookLoanItemForStaff::getBookId)
                .toList();
        List<Book> books = bookRepository.findAllById(bookIds);
        if (books.size() != bookIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Beberapa bookId tidak ditemukan");
        }

        // Loop tiap item dan bentuk LoanDetail
        for (BookLoanItemForStaff item : request.getItems()) {
            Book book = books.stream()
                    .filter(b -> b.getBookId().equals(item.getBookId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

            List<Copy> availableCopies = copyRepository
                    .findByBook_BookIdAndLocation_LocationIdAndStatus(
                            book.getBookId(), userLocation.getLocationId(), StatusCopies.AVAILABLE);

            if (availableCopies.size() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jumlah copy tidak cukup");
            }

            List<Copy> toLoan = new ArrayList<>(availableCopies.subList(0, item.getQuantity()));

            LoanDetail detail = new LoanDetail();
            detail.setLoan(loan);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setCopies(new ArrayList<>()); // penting banget!

            for (Copy copy : toLoan) {
                // Validasi lokasi copy
                if (!copy.getLocation().getLocationId().equals(userLocation.getLocationId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Copy bukan dari lokasi staff");
                }

                copy.setStatus(StatusCopies.ON_LOAN);
                copy.setLoanDetail(detail); // Set relasi balik

                detail.getCopies().add(copy); // masukkan copy ke detail
            }

            loan.getLoanDetails().add(detail); // masukkan detail ke loan
        }

        // Save and flush loan + cascade semuanya
        Loan savedLoan = loanRepository.saveAndFlush(loan); // PENTING: flush biar cascade langsung eksekusi

        return toLoanResponse(savedLoan);
    }
    @Transactional
    @Override
    public LoanResponse createLoanByMember(CreateLoanByMemberRequest request) {
        // Validate request
        validationService.validate(request);

        // Get current logged in member
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUser_Username(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // Get the copy being requested
        Copy copy = copyRepository.findById(request.getCopyId())
                .orElseThrow(() -> new NotFoundException("Copy not found with ID: " + request.getCopyId()));

        // Validate copy availability
        if (copy.getStatus() != StatusCopies.AVAILABLE) {
            throw new BadRequestException("Copy is not available for loan");
        }

        // Create new loan
        Loan loan = Loan.builder()
                .member(member)
                .loanDate(LocalDateTime.now())
                .status(LoanStatus.REQUESTED)
                .build();

        // Create loan detail
        LoanDetail loanDetail = LoanDetail.builder()
                .loan(loan)
                .book(copy.getBook())
                .quantity(1) // Member loans are always 1 copy at a time
                .copies(new ArrayList<>())
                .build();

        // Associate copy with loan detail
        copy.setStatus(StatusCopies.REQUESTED);
        copy.setLoanDetail(loanDetail);
        loanDetail.getCopies().add(copy);

        // Set relationships
        loan.setLoanDetails(List.of(loanDetail));

        // Save all entities
        Loan savedLoan = loanRepository.save(loan);

        return toLoanResponse(savedLoan);
    }
    @Transactional
    public LoanResponse verifyLoanRequest(String loanId, boolean approve) {
        // Get the loan request
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Loan not found with ID: " + loanId));

        // Validate loan status
        if (loan.getStatus() != LoanStatus.REQUESTED) {
            throw new BadRequestException("Loan is not in REQUESTED status");
        }

        // Get current staff user and location
        User staff = userService.getCurrentUser();
        if (staff.getRoles() != Role.STAFF) {  // Changed from contains() to == comparison
            throw new ForbiddenException("Only staff can verify loan requests");
        }

        Location staffLocation = staff.getLocation();
        if (staffLocation == null) {
            throw new BadRequestException("Staff location is not set");
        }


        if (approve) {
            // Approve the loan
            loan.setStatus(LoanStatus.ONGOING);
            loan.setLoanDate(LocalDateTime.now());
            loan.setDueDate(LocalDateTime.now().plusDays(loanDurationInDays));

            // Update copy status and validate location
            for (LoanDetail detail : loan.getLoanDetails()) {
                for (Copy copy : detail.getCopies()) {
                    if (!copy.getLocation().equals(staffLocation)) {
                        throw new BadRequestException(
                                "Copy location (" + copy.getLocation().getName() + ") " +
                                        "doesn't match staff location (" + staffLocation.getName() + ")");
                    }
                    copy.setStatus(StatusCopies.ON_LOAN);
                }
            }

            Optional<FinePrice> finePrice = finePriceRepository.findActiveFinePrices();


            if (finePrice.isPresent() && loan.getMember().getTelegramUser() != null) {
                long chatId = loan.getMember().getTelegramUser().getChatId();
                String bookTitle = loan.getCopy().getBook().getTitle();
                String fineAmount = finePrice.get().getPrice().toString();

                String message = String.format(
                        "✅ Peminjaman Anda untuk buku '%s' telah disetujui!\n\n" +
                                "Harap kembalikan sebelum tanggal: %s.\n\n" +
                                "ℹ️ Catatan: Jika terlambat, Anda akan dikenakan denda Rp %s per hari.",
                        bookTitle,
                        loan.getDueDate().toLocalDate().toString(),
                        fineAmount
                );
                libraryBot.sendMessageToUser(chatId, message);
            }

            Loan savedLoan = loanRepository.save(loan);
            return toLoanResponse(savedLoan);
        } else {
            // Reject the loan - return copies to available status
            for (LoanDetail detail : loan.getLoanDetails()) {
                for (Copy copy : detail.getCopies()) {
                    copy.setStatus(StatusCopies.AVAILABLE);
                    copy.setLoanDetail(null);
                    copyRepository.save(copy);
                }
            }

            if (loan.getMember().getTelegramUser() != null) {
                long chatId = loan.getMember().getTelegramUser().getChatId();
                String bookTitle = loan.getCopy().getBook().getTitle();
                String message = String.format(
                        "❌ Mohon maaf, peminjaman Anda untuk buku '%s' ditolak.",
                        bookTitle
                );
                libraryBot.sendMessageToUser(chatId, message);
            }

            // Delete the loan request
            loanRepository.delete(loan);
            return null;
        }
    }

    @Transactional
    @Override
    public LoanResponse returnLoan(String loanId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Location staffLocation = user.getLocation();

        // Ambil loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));

        // Validasi status
        if (loan.getStatus() != LoanStatus.ONGOING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan is not in ONGOING status");
        }

        // Update status copy
        for (LoanDetail detail : loan.getLoanDetails()) {
            for (Copy copy : detail.getCopies()) {
                // Validasi lokasi staff
                if (!copy.getLocation().getLocationId().equals(staffLocation.getLocationId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Copy is not from your location");
                }

                copy.setStatus(StatusCopies.AVAILABLE);
                copy.setLoanDetail(null); // hapus relasi balik
            }
            copyRepository.saveAll(detail.getCopies());
        }

        if (loan.getMember().getTelegramUser() != null) {
            long chatId = loan.getMember().getTelegramUser().getChatId();
            String bookTitle = loan.getCopy().getBook().getTitle();
            String message = String.format(
                    "✅ Pengembalian buku '%s' Anda telah dikonfirmasi. Terima kasih!",
                    bookTitle
            );
            libraryBot.sendMessageToUser(chatId, message);
        }

        String bookId = loan.getCopy().getBook().getBookId();
        // mengirimkan pengingat kalo buku sudah tersedia
        subscriptionService.notifySubscribers(bookId);

        // Update status loan
        loan.setStatus(LoanStatus.RETURNED); // or COMPLETED
        loanRepository.save(loan);

        return toLoanResponse(loan);
    }

    private LoanResponse toLoanResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setLoanId(loan.getLoanId());
        response.setMemberId(loan.getMember().getMemberId());
        response.setMemberName(loan.getMember().getName());
        response.setLoanDate(loan.getLoanDate());
        response.setDueDate(loan.getDueDate());
        response.setStatus(loan.getStatus());

        List<LoanDetailResponse> details = loan.getLoanDetails().stream()
                .map(this::toLoanDetailResponse)
                .collect(Collectors.toList());
        response.setDetails(details);

        return response;
    }

    private LoanDetailResponse toLoanDetailResponse(LoanDetail detail) {
        LoanDetailResponse response = new LoanDetailResponse();
        response.setBookId(detail.getBook().getBookId());
        response.setTitle(detail.getBook().getTitle());
        response.setQuantity(detail.getQuantity());

        List<CopyResponse> copies = detail.getCopies().stream()
                .map(this::toCopyResponse)
                .collect(Collectors.toList());
        response.setCopies(copies);

        return response;
    }

    private CopyResponse toCopyResponse(Copy copy) {
        CopyResponse response = new CopyResponse();
        response.setCopyId(copy.getCopyId());
        response.setRackCode(copy.getRackCode());
        response.setStatus(copy.getStatus());

        BookResponse bookResponse = toBookResponse(copy.getBook());
        response.setBook(bookResponse);

        LocationResponse locationResponse = new LocationResponse();
        locationResponse.setLocationId(copy.getLocation().getLocationId());
        locationResponse.setName(copy.getLocation().getName());
        locationResponse.setAddress(copy.getLocation().getAddress());
        locationResponse.setDescription(copy.getLocation().getDescription());
        response.setLocation(locationResponse);

        return response;
    }

    private BookResponse toBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getBookId());
        response.setIsbn(book.getIsbn());
        response.setTitle(book.getTitle());
        response.setPublisherName(book.getPublisher().getName());
        response.setAuthors(toAuthorResponses(book.getAuthor()));
        response.setCategories(toCategoryResponses(book.getCategory()));
        response.setPublicationYear(book.getPublicationYear());
        response.setLanguage(book.getLanguage());
        response.setImageUrl(book.getImageUrl());
        response.setPageCount(book.getPageCount());
        return response;
    }

    private List<AuthorResponse> toAuthorResponses(Set<Author> authors) {
        return authors.stream()
                .map(this::toAuthorResponse)
                .collect(Collectors.toList());
    }

    private AuthorResponse toAuthorResponse(Author author) {
        AuthorResponse response = new AuthorResponse();
        response.setAuthorId(author.getAuthorId());
        response.setName(author.getName());
        return response;
    }

    private List<CategoryResponse> toCategoryResponses(Set<Category> categories) {
        return categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse toCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setCategoryId(category.getId());
        response.setName(category.getName());
        return response;
    }



    @Transactional
    @Override
    public LoanResponse update(String loanId, UpdateLoanRequest request) {
        validationService.validate(request);

        // Ambil user login dan lokasinya
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Location staffLocation = currentUser.getLocation();

        // Ambil loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan tidak ditemukan"));

        // Ambil semua LoanDetail yang sudah ada
        List<LoanDetail> existingDetails = loan.getLoanDetails();

        // Balikkan semua copy sebelumnya ke status AVAILABLE dan putuskan relasinya
        for (LoanDetail detail : existingDetails) {
            for (Copy c : detail.getCopies()) {
                c.setStatus(StatusCopies.AVAILABLE);
                c.setLoanDetail(null);
            }
            copyRepository.saveAll(detail.getCopies());
        }

        // Hapus detail lama
        loanDetailRepository.deleteAll(existingDetails);

        // Ambil semua book
        List<String> bookIds = request.getItems().stream()
                .map(BookLoanItemForStaff::getBookId).toList();
        List<Book> books = bookRepository.findAllById(bookIds);

        if (books.size() != bookIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Beberapa bookId tidak valid");
        }

        List<LoanDetail> newDetails = new ArrayList<>();

        // Validasi stok dan ambil copy baru
        for (BookLoanItemForStaff item : request.getItems()) {
            long available = copyRepository.countByBook_BookIdAndLocation_LocationIdAndStatus(
                    item.getBookId(), staffLocation.getLocationId(), StatusCopies.AVAILABLE);
            if (item.getQuantity() > available) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Stok buku %s hanya %d, diminta %d", item.getBookId(), available, item.getQuantity()));
            }

            Book book = books.stream()
                    .filter(b -> b.getBookId().equals(item.getBookId()))
                    .findFirst()
                    .orElseThrow();

            List<Copy> availableCopies = copyRepository
                    .findByBook_BookIdAndLocation_LocationIdAndStatus(
                            book.getBookId(), staffLocation.getLocationId(), StatusCopies.AVAILABLE);

            List<Copy> toLoan = availableCopies.subList(0, item.getQuantity());

            // Buat detail baru
            LoanDetail detail = new LoanDetail();
            detail.setLoan(loan);
            detail.setBook(book);
            detail.setQuantity(item.getQuantity());
            detail.setCopies(toLoan);

            for (Copy c : toLoan) {
                c.setStatus(StatusCopies.ON_LOAN);
                c.setLoanDetail(detail);
            }

            copyRepository.saveAll(toLoan);
            newDetails.add(detail);
        }

        loanDetailRepository.saveAll(newDetails);
        loan.setLoanDetails(newDetails);

        // Update status jika dikirim dari request
        if (request.getStatus() != null) {
            loan.setStatus(request.getStatus());
        }

        return toLoanResponse(loan);
    }

    @Override
    public LoanResponse getById(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan tidak ditemukan"));

        return toLoanResponse(loan);
    }

    @Override
    public Page<LoanResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Loan> loanPage = loanRepository.findAll(pageable);

        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse)
                .toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

    @Override
    @Transactional
    public void requestLoan(User user, Long copyId) {
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Salinan buku tidak ditemukan"));

        if (copy.getStatus() != StatusCopies.AVAILABLE) {
            throw new RuntimeException("Salinan buku tidak tersedia untuk dipinjam");
        }

        copy.setStatus(StatusCopies.REQUESTED);
        copyRepository.save(copy);

        Loan loan = new Loan();
        loan.setMember(user.getMember());
        loan.setCopy(copy);
        loan.setLoanDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(loanDurationInDays));
        loan.setStatus(LoanStatus.REQUESTED);

        loanRepository.save(loan);
    }

    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 jam
    @Transactional
    @Override
    public void checkExpiredLoanRequests() {
        log.info("Menjalankan tugas pengecekan permintaan pinjaman kedaluwarsa...");
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Loan> expiredRequests = loanRepository.findAllByStatusAndLoanDateBefore(LoanStatus.REQUESTED, oneDayAgo);

        for (Loan loan : expiredRequests) {
            log.warn("Membatalkan permintaan pinjaman ID: {} karena kedaluwarsa.", loan.getLoanId());
            loan.setStatus(LoanStatus.CANCELLED);

            Copy copy = loan.getCopy();
            if (copy != null && copy.getStatus() == StatusCopies.REQUESTED) {
                copy.setStatus(StatusCopies.AVAILABLE);
                copyRepository.save(copy);
            }
        }
        loanRepository.saveAll(expiredRequests);
    }

    @Override
    public Page<Loan> findActiveLoansByMemberId(String memberId, Pageable pageable) {
        return loanRepository.findActiveLoansByMemberId(memberId, pageable);
    }

    @Override
    @Transactional
    public Loan requestReturn(String loanId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pinjaman tidak ditemukan."));

        if (loan.getStatus() != LoanStatus.ONGOING && loan.getStatus() != LoanStatus.LATE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buku tidak dalam status bisa dikembalikan.");
        }

        loan.setStatus(LoanStatus.RETURN_REQUESTED);
        loan.setReturnRequestDate(LocalDateTime.now());

        Copy copy = loan.getCopy();
        copy.setStatus(StatusCopies.RETURN_REQUESTED);

        copyRepository.save(copy);
        loanRepository.save(loan);

        return loan;
    }

    @Override
    public Loan findById(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data pinjaman tidak ditemukan"));
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void revertExpiredReturnRequests() {
        log.info("Menjalankan tugas pengecekan pengajuan pengembalian kedaluwarsa...");

        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);
//        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(1);

        List<Loan> expiredRequests = loanRepository.findAllByStatusAndReturnRequestDateBefore(LoanStatus.RETURN_REQUESTED, expirationTime);

        if (expiredRequests.isEmpty()) {
            log.info("Tidak ada pengajuan pengembalian yang kedaluwarsa.");
            return;
        }

        log.warn("Ditemukan {} pengajuan pengembalian kedaluwarsa. Mengembalikan status...", expiredRequests.size());

        for (Loan loan : expiredRequests) {
            String bookTitle = loan.getCopy().getBook().getTitle();

            if (LocalDateTime.now().isAfter(loan.getDueDate())) {
                loan.setStatus(LoanStatus.LATE);
                loan.getCopy().setStatus(StatusCopies.ON_LOAN);
            } else {
                loan.setStatus(LoanStatus.ONGOING);
                loan.getCopy().setStatus(StatusCopies.ON_LOAN);
            }

            loan.setReturnRequestDate(null);

            log.info("Status pinjaman ID {} dikembalikan menjadi {}.", loan.getLoanId(), loan.getStatus());

            if (loan.getMember() != null && loan.getMember().getTelegramUser() != null) {
                Long chatId = loan.getMember().getTelegramUser().getChatId();
                String messageText = String.format(
                        "❗️ Pengajuan pengembalian untuk buku '%s' telah dibatalkan.\n\n" +
                                "Anda tidak menyerahkan buku dalam batas waktu 24 jam. " +
                                "Status peminjaman Anda telah dikembalikan menjadi **%s**.",
                        bookTitle,
                        loan.getStatus().toString()
                );

                SendMessage notification = new SendMessage(String.valueOf(chatId), messageText);
                notification.setParseMode("Markdown");

                try {
                    libraryBot.execute(notification);
                    log.info("Notifikasi pembatalan pengajuan terkirim ke chatId: {}", chatId);
                } catch (TelegramApiException e) {
                    log.error("Gagal mengirim notifikasi pembatalan ke chatId: {}", chatId, e);
                }
            }
        }

        loanRepository.saveAll(expiredRequests);
    }

    @Override
    @Transactional
    public Loan extendLoan(String loanId) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Pinjaman tidak ditemukan."));

        if (loan.isHasBeenExtended()) {
            throw new Exception("Pinjaman ini sudah pernah diperpanjang.");
        }

        Book book = loan.getCopy().getBook();
        if (notificationRepository.existsByBookAndIsNotifiedFalse(book)) {
            throw new Exception("Tidak dapat diperpanjang, buku ini sedang dalam antrean oleh anggota lain.");
        }

        int extensionDays = 7;
        loan.setDueDate(loan.getDueDate().plusDays(extensionDays));
        loan.setHasBeenExtended(true);

        log.info("Pinjaman {} untuk buku '{}' berhasil diperpanjang hingga {}",
                loanId, book.getTitle(), loan.getDueDate());

        return loanRepository.save(loan);
    }

    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void sendDueDateReminders() {
        log.info("Menjalankan tugas pengiriman notifikasi jatuh tempo...");

        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        List<Loan> upcomingLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.ONGOING, threeDaysFromNow);

        for (Loan loan : upcomingLoans) {
            if (!loan.isHasBeenExtended() && loan.getDueDate().isAfter(LocalDateTime.now())) {

                Long chatId = loan.getMember().getTelegramUser().getChatId();
                String bookTitle = loan.getCopy().getBook().getTitle();
                String dueDate = loan.getDueDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

                String messageText = String.format(
                        "🔔 *Pengingat Jatuh Tempo*\n\n" +
                                "Peminjaman buku '%s' akan jatuh tempo pada *%s*.\n\n" +
                                "Anda dapat memperpanjang masa pinjaman satu kali.", bookTitle, dueDate
                );

                InlineKeyboardButton extendButton = new InlineKeyboardButton("Perpanjang Pinjaman (7 Hari)");
                extendButton.setCallbackData("extend_loan:" + loan.getLoanId());
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(List.of(List.of(extendButton)));

                SendMessage message = new SendMessage(String.valueOf(chatId), messageText);
                message.setParseMode("Markdown");
                message.setReplyMarkup(keyboard);

                try {
                    libraryBot.execute(message);
                    log.info("Notifikasi pengingat terkirim untuk pinjaman {}", loan.getLoanId());
                } catch (Exception e) {
                    log.error("Gagal mengirim notifikasi untuk pinjaman {}", loan.getLoanId(), e);
                }
            }
        }
    }

    @Override
    @Transactional
    public void confirmReturn(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pinjaman tidak ditemukan."));

        if (loan.getStatus() != LoanStatus.RETURN_REQUESTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pinjaman ini tidak dalam status pengajuan pengembalian.");
        }

        loan.setStatus(LoanStatus.RETURNED);

        Copy copy = loan.getCopy();
        copy.setStatus(StatusCopies.AVAILABLE);

        copyRepository.save(copy);
        loanRepository.save(loan);

        String bookId = copy.getBook().getBookId();
        log.info("Buku {} (copyId: {}) telah dikembalikan. Memicu notifikasi untuk subscriber.", bookId, copy.getCopyId());
        subscriptionService.notifySubscribers(bookId);
    }

    @Override
    public Page<LoanResponse> getByMember(String memberId, int page, int size) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException(
                   "History loan from memberID " + memberId + " not found."
                )
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findByMember(member, pageable);

        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse).toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

    @Override
    public Page<LoanResponse> getByBook(String bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findByBookId(bookId, pageable);
        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse).toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

    @Override
    public Page<LoanResponse> getByStatus(String statusReq, int page, int size) {
        LoanStatus status;
        if (statusReq.equalsIgnoreCase("requested")) {
            status = LoanStatus.REQUESTED;
        } else if (statusReq.equalsIgnoreCase("ongoing")) {
            status = LoanStatus.ONGOING;
        } else if (statusReq.equalsIgnoreCase("returned")) {
            status = LoanStatus.RETURNED;
        } else if (statusReq.equalsIgnoreCase("late")) {
            status = LoanStatus.LATE;
        } else if (statusReq.equalsIgnoreCase("canceled")) {
            status = LoanStatus.CANCELLED;
        } else {
            status = LoanStatus.RETURN_REQUESTED;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findByStatus(status, pageable);
        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse).toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

    @Override
    public Page<LoanResponse> getByDueDate(String dueDate, int page, int size) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dueDate, formatter);
        LocalDateTime startOfDay = date.atStartOfDay();

        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findByDueDate(startOfDay, pageable);

        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse).toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

    @Override
    public Page<LoanResponse> getByLocation(Long locationId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Loan> loanPage = loanRepository.findByLocationId(locationId, pageable);
        List<LoanResponse> responseList = loanPage.getContent().stream()
                .map(this::toLoanResponse).toList();
        return new PageImpl<>(responseList, pageable, loanPage.getTotalElements());
    }

//    private LoanResponse toLoanResponse(Loan loan) {
//        return LoanResponse.builder()
//                .loanId(loan.getLoanId())
//                .memberId(loan.getMember().getMemberId())
//                .memberName(loan.getMember().getName())
//                .loanDate(loan.getLoanDate())
//                .dueDate(loan.getDueDate())
//                .status(loan.getStatus())
//                .details(
//                        loan.getLoanDetails().stream()
//                                .map(detail -> LoanDetailResponse.builder()
//                                        .bookId(detail.getBook().getBookId())
//                                        .title(detail.getBook().getTitle())
//                                        .quantity(detail.getQuantity())
//                                        .build())
//                                .toList()
//                )
//                .build();
//    }
    @Transactional(readOnly = true)
    @Override
    public Page<LoanReportResponse> generateLoanReport(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Long locId = user.getLocation().getLocationId();
        Page<Loan> loanPage = loanRepository.findReportByLocationAndStatus(locId, LoanStatus.ONGOING, pageable);
        System.out.println("Found loans: " + loanPage.getTotalElements());

        List<LoanReportResponse> reports = loanPage.getContent().stream()
                // filter loans yang punya detail _dan_ punya copy
                .filter(loan -> {
                    List<LoanDetail> details = loan.getLoanDetails();
                    if (details == null || details.isEmpty()) return false;
                    List<Copy> copies = details.get(0).getCopies();
                    return copies != null && !copies.isEmpty();
                })
                // map langsung di sini, tanpa melempar
                .map(loan -> {
                    LoanDetail d = loan.getLoanDetails().get(0);
                    Copy c = d.getCopies().get(0);
                    return LoanReportResponse.builder()
                            .loanId(loan.getLoanId())
                            .memberName(loan.getMember().getName())
                            .memberEmail(loan.getMember().getEmail())
                            .bookTitle(d.getBook().getTitle())
                            .quantity(d.getQuantity())
                            .loanDate(loan.getLoanDate())
                            .dueDate(loan.getDueDate())
                            .status(loan.getStatus())
                            .locationName(c.getLocation().getName())
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(reports, pageable, reports.size());
    }
    private LoanReportResponse toReport(Loan loan) {
        List<LoanDetail> details = loan.getLoanDetails();
        if (details == null || details.isEmpty()) {
            throw new IllegalStateException("Loan does not contain LoanDetails");
        }

        LoanDetail firstDetail = details.get(0);
        List<Copy> copies = firstDetail.getCopies();
        if (copies == null || copies.isEmpty()) {
            throw new IllegalStateException("LoanDetail does not contain Copies");
        }

        return LoanReportResponse.builder()
                .loanId(loan.getLoanId())
                .memberName(loan.getMember().getName())
                .memberEmail(loan.getMember().getEmail())
                .bookTitle(firstDetail.getBook().getTitle())
                .quantity(firstDetail.getQuantity())
                .loanDate(loan.getLoanDate())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .locationName(copies.get(0).getLocation().getName())
                .build();
    }
}
