package com.enigma.library_app.controller;

import com.enigma.library_app.service.testing.StaffLoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/loans")
@RequiredArgsConstructor
public class StaffLoanController {

    private final StaffLoanService staffLoanService;

    @PutMapping("/{loanId}/verify")
    public ResponseEntity<String> verifyLoan(
            @PathVariable String loanId,
            @RequestParam boolean approve) {

        staffLoanService.verifyLoan(loanId, approve);
        return ResponseEntity.ok("Loan verification processed successfully");
    }

    @PutMapping("/{loanId}/return")
    public ResponseEntity<String> verifyReturn(@PathVariable String loanId) {
        staffLoanService.verifyReturn(loanId);
        return ResponseEntity.ok("Return verification processed successfully, availability notifications sent.");
    }
}