package com.bfsi.customer;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long customerId;

    @NotBlank
    private String accountType; // SAVINGS, CURRENT

    @NotNull
    private BigDecimal balance;

    public Account() {}

    public Account(Long customerId, String accountType, BigDecimal balance) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }

    public Long getId() { return id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}