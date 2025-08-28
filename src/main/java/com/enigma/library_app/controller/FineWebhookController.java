package com.enigma.library_app.controller;

import com.enigma.library_app.service.contract.fine.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class FineWebhookController {

    private final FineService fineService;

    @PostMapping("/midtrans/fines")
    public ResponseEntity<String> handleMidtransNotification(@RequestBody Map<String, Object> payload) {
        fineService.handlePaymentNotification(payload);
        return new ResponseEntity<>("Notification received.", HttpStatus.OK);
    }
}
