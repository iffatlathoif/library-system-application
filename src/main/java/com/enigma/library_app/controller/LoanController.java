package com.enigma.library_app.controller;

import com.enigma.library_app.dto.BaseResponse;
import com.enigma.library_app.dto.PagingResponse;
import com.enigma.library_app.dto.loan.request.CreateLoanByMemberRequest;
import com.enigma.library_app.dto.loan.request.CreateLoanByStaffRequest;
import com.enigma.library_app.dto.loan.request.UpdateLoanRequest;
import com.enigma.library_app.dto.loan.response.LoanReportResponse;
import com.enigma.library_app.dto.loan.response.LoanResponse;
import com.enigma.library_app.service.LoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
public class LoanController {
    @Autowired
    private LoanService loanService;
    @PreAuthorize("hasAuthority('STAFF')")
    @PostMapping(
            path = "/api/loan",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse>createLoanByStaff(@RequestBody CreateLoanByStaffRequest request){
        LoanResponse loanResponse = loanService.createLoanByStaff(request);
        return BaseResponse.<LoanResponse>builder().data(loanResponse).build();
    }
    @PreAuthorize("hasAuthority('STAFF')")
    @PutMapping(
            path = "/api/loan/{loanId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse> update(@RequestBody UpdateLoanRequest request,
                                               @PathVariable("loanId") String loanId){
        LoanResponse loanResponse = loanService.update(loanId, request);
        return BaseResponse.<LoanResponse>builder().data(loanResponse).build();
    }
    @PreAuthorize("hasAuthority('MEMBER')")
    @PostMapping(
            path = "/api/loan/member",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse> createLoanByMember(@RequestBody CreateLoanByMemberRequest request) {
        LoanResponse loanResponse = loanService.createLoanByMember(request);
        return BaseResponse.<LoanResponse>builder().data(loanResponse).build();
    }
    @PreAuthorize("hasAuthority('STAFF')")
    @PostMapping(
            path = "/api/loan/{loanId}/return",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse> returnLoan(@PathVariable("loanId") String loanId) {
        log.info("Return loan called with ID: {}", loanId);
        LoanResponse loanResponse = loanService.returnLoan(loanId);
        return BaseResponse.<LoanResponse>builder().data(loanResponse).build();
    }
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN')")
    @GetMapping("/api/loan")
    public BaseResponse<List<LoanResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<LoanResponse> responsePage = loanService.getAll(page, size);
        PagingResponse paging = PagingResponse.builder()
                .currentPage(responsePage.getNumber())
                .totalPage(responsePage.getTotalPages())
                .size(responsePage.getSize())
                .build();

        return BaseResponse.<List<LoanResponse>>builder()
                .data(responsePage.getContent())
                .paging(paging)
                .build();
    }
    @PreAuthorize("hasAnyAuthority('STAFF','ADMIN','MEMBER')")
    @GetMapping(
            path = "/api/loan/{loanId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse> getById(@PathVariable("loanId") String loanId){
        LoanResponse loanResponse = loanService.getById(loanId);
        return BaseResponse.<LoanResponse>builder().data(loanResponse).build();
    }
    @PreAuthorize("hasAuthority('STAFF')")
    @PostMapping(
            path = "/api/loan/{loanId}/verify",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public BaseResponse<LoanResponse> verifyLoanRequest(
            @PathVariable String loanId,
            @RequestParam boolean approve) {
        LoanResponse res = loanService.verifyLoanRequest(loanId, approve);
        if (approve) {
            return BaseResponse.success(res);
        } else {
            return BaseResponse.success(null); // atau pesan “Rejected”
        }
    }
    @GetMapping("/api/loan/report")
    public BaseResponse<Page<LoanReportResponse>> getLoanReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LoanReportResponse> pageResult = loanService.generateLoanReport(pageable);

        PagingResponse paging = PagingResponse.builder()
                .currentPage(pageResult.getNumber())
                .totalPage(pageResult.getTotalPages())
                .size(pageResult.getSize())
                .build();

        return BaseResponse.successWithPaging(pageResult, paging);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/loan/history/member/{memberId}")
    public BaseResponse<List<LoanResponse>> getByMember(
        @PathVariable("memberId") String memberId,
        @RequestParam(value = "page", required = false,defaultValue = "0") Integer page,
        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ){
        Page<LoanResponse> loanResponses = loanService.getByMember(memberId, page, size);
        return BaseResponse.<List<LoanResponse>>builder()
                .data(loanResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(loanResponses.getNumber())
                        .totalPage(loanResponses.getTotalPages())
                        .size(loanResponses.getSize())
                        .build())
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/loan/history/book/{bookId}")
    public BaseResponse<List<LoanResponse>> getByBook(
            @PathVariable("bookId") String bookId,
            @RequestParam(value = "page", required = false,defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ){
        Page<LoanResponse> loanResponses = loanService.getByBook(bookId, page, size);
        return BaseResponse.<List<LoanResponse>>builder()
                .data(loanResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(loanResponses.getNumber())
                        .totalPage(loanResponses.getTotalPages())
                        .size(loanResponses.getSize())
                        .build())
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/loan/history/status")
    public BaseResponse<List<LoanResponse>> getByStatus(
            @RequestParam(value = "status") String status,
            @RequestParam(value = "page", required = false,defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ){
        Page<LoanResponse> loanResponses = loanService.getByStatus(status, page, size);
        return BaseResponse.<List<LoanResponse>>builder()
                .data(loanResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(loanResponses.getNumber())
                        .totalPage(loanResponses.getTotalPages())
                        .size(loanResponses.getSize())
                        .build())
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/loan/history/duedate")
    public BaseResponse<List<LoanResponse>> getByDueDate(
            @RequestParam(value = "dueDate") String dueDate,
            @RequestParam(value = "page", required = false,defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ){
        Page<LoanResponse> loanResponses = loanService.getByDueDate(dueDate, page, size);
        return BaseResponse.<List<LoanResponse>>builder()
                .data(loanResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(loanResponses.getNumber())
                        .totalPage(loanResponses.getTotalPages())
                        .size(loanResponses.getSize())
                        .build())
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/api/loan/history/location/{locationId}")
    public BaseResponse<List<LoanResponse>> getByLocation(
            @PathVariable("locationId") Long locationId,
            @RequestParam(value = "page", required = false,defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ){
        Page<LoanResponse> loanResponses = loanService.getByLocation(locationId, page, size);
        return BaseResponse.<List<LoanResponse>>builder()
                .data(loanResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(loanResponses.getNumber())
                        .totalPage(loanResponses.getTotalPages())
                        .size(loanResponses.getSize())
                        .build())
                .build();
    }
}
