package com.bfsi.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private static final String INTERNAL_TOKEN = "bfsi-internal-dev-token";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RestClient restClient = RestClient.create();

    public TransferController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public static class PaymentRequest {
        public Long fromAccountId;
        public Long toAccountId;
        public BigDecimal amount;

        public PaymentRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
            this.amount = amount;
        }
    }

    @PostMapping
    @Transactional
    public Transaction transfer(@Valid @RequestBody TransferRequestDto req) {
        if (req.fromAccountId.equals(req.toAccountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId and toAccountId must be different");
        }

        Account from = accountRepository.findById(req.fromAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "fromAccount not found"));
        Account to = accountRepository.findById(req.toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "toAccount not found"));

        if (from.getBalance() == null) from.setBalance(BigDecimal.ZERO);
        if (to.getBalance() == null) to.setBalance(BigDecimal.ZERO);

        if (from.getBalance().compareTo(req.amount) < 0) {
            return transactionRepository.save(new Transaction(req.fromAccountId, req.toAccountId, req.amount, "FAILED", Instant.now()));
        }

        // payment-service call: any 4xx/5xx/network error => FAILED (no 500 to client)
        String paymentStatus = "FAILED";
        try {
            paymentStatus = restClient.post()
                    .uri("http://127.0.0.1:8082/payments/process")
                    .header("X-Internal-Token", INTERNAL_TOKEN)
                    .body(new PaymentRequest(req.fromAccountId, req.toAccountId, req.amount))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            paymentStatus = "FAILED";
        }

        if (!"SUCCESS".equalsIgnoreCase(paymentStatus)) {
            return transactionRepository.save(new Transaction(req.fromAccountId, req.toAccountId, req.amount, "FAILED", Instant.now()));
        }

        from.setBalance(from.getBalance().subtract(req.amount));
        to.setBalance(to.getBalance().add(req.amount));

        accountRepository.save(from);
        accountRepository.save(to);

        return transactionRepository.save(new Transaction(req.fromAccountId, req.toAccountId, req.amount, "SUCCESS", Instant.now()));
    }
}