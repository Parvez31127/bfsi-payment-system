package com.bfsi.customer;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RestClient restClient = RestClient.create();

    public TransferController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public static class TransferRequest {
        @NotNull public Long fromAccountId;
        @NotNull public Long toAccountId;
        @NotNull public BigDecimal amount;
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
    public Transaction transfer(@RequestBody TransferRequest req) {
        if (req.fromAccountId == null || req.toAccountId == null || req.amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId, toAccountId, amount are required");
        }
        if (req.fromAccountId.equals(req.toAccountId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromAccountId and toAccountId must be different");
        }
        if (req.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be > 0");
        }

        Account from = accountRepository.findById(req.fromAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "fromAccount not found"));
        Account to = accountRepository.findById(req.toAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "toAccount not found"));

        if (from.getBalance() == null) from.setBalance(BigDecimal.ZERO);
        if (to.getBalance() == null) to.setBalance(BigDecimal.ZERO);

        if (from.getBalance().compareTo(req.amount) < 0) {
            Transaction failed = new Transaction(req.fromAccountId, req.toAccountId, req.amount, "FAILED", Instant.now());
            return transactionRepository.save(failed);
        }

        // Call payment-service to approve/process
        String paymentStatus = restClient.post()
                .uri("http://127.0.0.1:8082/payments/process")
                .body(new PaymentRequest(req.fromAccountId, req.toAccountId, req.amount))
                .retrieve()
                .body(String.class);

        if (!"SUCCESS".equalsIgnoreCase(paymentStatus)) {
            Transaction failed = new Transaction(req.fromAccountId, req.toAccountId, req.amount, "FAILED", Instant.now());
            return transactionRepository.save(failed);
        }

        // Apply balance changes only after SUCCESS
        from.setBalance(from.getBalance().subtract(req.amount));
        to.setBalance(to.getBalance().add(req.amount));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction success = new Transaction(req.fromAccountId, req.toAccountId, req.amount, "SUCCESS", Instant.now());
        return transactionRepository.save(success);
    }
}