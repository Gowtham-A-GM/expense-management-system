package com.example.expensetracker.repository

import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseRequest
import com.example.expensetracker.network.RetrofitClient

class ExpenseRepository {

    private val api = RetrofitClient.api

    suspend fun getExpenses(userId: String): List<Expense> {
        return api.getExpenses(userId)
    }

    suspend fun addExpense(req: ExpenseRequest): String {
        return api.addExpense(req)
    }

    suspend fun updateExpense(id: String, req: ExpenseRequest) {
        api.updateExpense(id, req)
    }

    suspend fun deleteExpense(id: String) {
        api.deleteExpense(id)
    }
}
