package com.bfsi.payment;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    public static class PaymentRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public BigDecimal amount;
    }

    @PostMapping("/process")
    public String process(@RequestBody PaymentRequest req) {
        if (req == null || req.amount == null || req.amount.signum() <= 0) {
            return "FAILED";
        }
        // For now: simulate success. Later we can add rules, fraud checks, limits, etc.
        return "SUCCESS";
    }

    @GetMapping("/ping")
    public String ping() {
        return "payment pong";
    }
}