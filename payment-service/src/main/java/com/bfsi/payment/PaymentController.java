package com.bfsi.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private static final String INTERNAL_TOKEN = "bfsi-internal-dev-token";

    public static class PaymentRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public BigDecimal amount;
    }

    @PostMapping("/process")
    public String process(@RequestBody PaymentRequest req,
                          @RequestHeader(value = "X-Internal-Token", required = false) String internalToken) {

        if (!"bfsi-internal-dev-token".equals(internalToken)) {
            return "FAILED";
        }

        if (req == null || req.amount == null || req.amount.signum() <= 0) {
            return "FAILED";
        }
        return "SUCCESS";
    }
    @GetMapping("/ping")
    public String ping() {
        return "payment pong";
    }
}