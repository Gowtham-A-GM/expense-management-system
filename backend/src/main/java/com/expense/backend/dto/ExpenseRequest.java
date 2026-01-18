package com.expense.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpenseRequest { // this file is used to receive data from app

    @NotNull
    private Double amount;

    @NotBlank
    private String description;

    @NotBlank
    private String date;

    @NotBlank
    private String category;

    @NotBlank
    private String userId;
}
