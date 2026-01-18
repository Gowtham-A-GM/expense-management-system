package com.example.expensetracker.model

data class ExpenseRequest(
    val amount: Double,
    val description: String,
    val date: String,
    val category: String,
    val userId: String
)
