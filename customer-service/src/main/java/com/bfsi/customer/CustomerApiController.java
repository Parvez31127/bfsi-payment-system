package com.bfsi.customer;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerApiController {

    private final CustomerRepository repository;

    public CustomerApiController(CustomerRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Customer create(@Valid @RequestBody Customer customer) {
        return repository.save(customer);
    }

    @GetMapping
    public List<Customer> getAll() {
        return repository.findAll();
    }
}