package com.example.expensetracker.network

import com.example.expensetracker.api.ExpenseApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    val api: ExpenseApi = retrofit.create(ExpenseApi::class.java)
}