package com.bfsi.customer;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountApiController {

    private final AccountRepository repository;

    public AccountApiController(AccountRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Account open(@Valid @RequestBody Account account) {
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        return repository.save(account);
    }

    @GetMapping
    public List<Account> getByCustomerId(@RequestParam Long customerId) {
        return repository.findByCustomerId(customerId);
    }
}