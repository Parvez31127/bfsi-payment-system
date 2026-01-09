package com.bfsi.customer;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionApiController {

    private final TransactionRepository repository;

    public TransactionApiController(TransactionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Transaction> byAccount(@RequestParam Long accountId) {
        return repository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }
}