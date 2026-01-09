package com.bfsi.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequestDto {

    @NotNull
    public Long fromAccountId;

    @NotNull
    public Long toAccountId;

    @NotNull
    @Positive
    public BigDecimal amount;
}