package com.example.expensetracker.api

import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseRequest
import okhttp3.Response
import retrofit2.http.*

interface ExpenseApi {

    @POST("expenses")
    suspend fun addExpense(@Body req: ExpenseRequest): String

    @GET("expenses")
    suspend fun getExpenses(
        @Query("userId") userId: String,
        @Query("category") category: String? = null
    ): List<Expense>

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body req: ExpenseRequest
    )

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: String
    )

}
