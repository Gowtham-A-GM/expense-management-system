package com.example.expensetracker.model

data class Expense(
    val id: String = "",
    val category: String = "",
    val description: String = "",
    val date: String = "",
    val amount: Double = 0.0
)
