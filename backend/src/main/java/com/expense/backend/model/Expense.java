package com.expense.backend.model;

import lombok.Data;

@Data // to auto generate the getters, setters
public class Expense {
    private String id;
    private String userId;
    private Double amount;
    private String description;
    private String date;
    private String category;
}
