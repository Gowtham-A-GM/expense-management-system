package com.example.expensetracker.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expensetracker.model.Expense
import com.example.expensetracker.repository.ExpenseRepository
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.model.ExpenseRequest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = ExpenseRepository()

    private val allExpenses = mutableListOf<Expense>()

    private val _expensesList = MutableLiveData<List<Expense>>()
    val expensesList: LiveData<List<Expense>> = _expensesList

    private val _selectedCategory = MutableLiveData("All")
    val selectedCategory: LiveData<String> = _selectedCategory

    private val _selectedDate = MutableLiveData<String?>(null)
    val selectedDate: LiveData<String?> = _selectedDate

    private val _deleteMessage = MutableLiveData<String?>()
    val deleteMessage: LiveData<String?> = _deleteMessage

    private val _foodTotal = MutableLiveData(0.0)
    val foodTotal: LiveData<Double> = _foodTotal

    private val _travelTotal = MutableLiveData(0.0)
    val travelTotal: LiveData<Double> = _travelTotal

    private val _shoppingTotal = MutableLiveData(0.0)
    val shoppingTotal: LiveData<Double> = _shoppingTotal

    private val _othersTotal = MutableLiveData(0.0)
    val othersTotal: LiveData<Double> = _othersTotal

    private val _overallTotal = MutableLiveData(0.0)
    val overallTotal: LiveData<Double> = _overallTotal


    // LOAD ALL EXPENSES
    fun loadExpenses(userId: String) {
        viewModelScope.launch {
            try {
                val list = repo.getExpenses(userId)

                allExpenses.clear()
                allExpenses.addAll(list)

                filterExpenses()

            } catch (e: Exception) {
                Log.e("DEBUG_VM", "Load error: ${e.message}")
            }
        }
    }


    // ADD EXPENSE
    fun addExpense(expense: Expense, userId: String) {
        viewModelScope.launch {
            try {
                val req = ExpenseRequest(
                    amount = expense.amount,
                    description = expense.description,
                    date = expense.date,
                    category = expense.category,
                    userId = userId
                )

                repo.addExpense(req)

                loadExpenses(userId)

            } catch (e: Exception) {
                Log.e("DEBUG_VM", "Add error: ${e.message}")
            }
        }
    }


    // DELETE EXPENSE
    fun deleteExpense(expense: Expense, userId: String) {
        viewModelScope.launch {
            try {
                repo.deleteExpense(expense.id)

                _deleteMessage.value = "${expense.description} deleted"

                loadExpenses(userId)

            } catch (e: Exception) {
                Log.e("DEBUG_VM", "Delete error: ${e.message}")
            }
        }
    }


    // UPDATE EXPENSE
    fun updateExpense(updated: Expense, userId: String) {
        viewModelScope.launch {
            try {
                val req = ExpenseRequest(
                    amount = updated.amount,
                    description = updated.description,
                    date = updated.date,
                    category = updated.category,
                    userId = userId
                )

                repo.updateExpense(updated.id, req)

                _deleteMessage.value = "${updated.description} updated"

                loadExpenses(userId)

            } catch (e: Exception) {
                Log.e("DEBUG_VM", "Update error: ${e.message}")
            }
        }
    }


    // FILTER + SUMMARY
    private fun filterExpenses() {
        var result = allExpenses.toList()

        // CATEGORY FILTER
        selectedCategory.value?.let { cat ->
            if (cat != "All") {   // <--- FIX HERE
                result = result.filter { it.category == cat }
            }
        }

        // DATE FILTER
        selectedDate.value?.let { date ->
            if (!date.isNullOrEmpty())
                result = result.filter { it.date == date }
        }

        // Update list
        _expensesList.value = result

        // Update totals from all data (not filtered ones)
        _foodTotal.value = allExpenses.filter { it.category == "Food" }.sumOf { it.amount }
        _travelTotal.value = allExpenses.filter { it.category == "Travel" }.sumOf { it.amount }
        _shoppingTotal.value = allExpenses.filter { it.category == "Shopping" }.sumOf { it.amount }
        _othersTotal.value = allExpenses.filter { it.category == "Others" }.sumOf { it.amount }

        _overallTotal.value = allExpenses.sumOf { it.amount }
    }



    fun changeCategory(cat: String) {
        _selectedCategory.value = cat
        filterExpenses()
    }

    fun changeDate(date: String) {
        _selectedDate.value = date
        filterExpenses()
    }

    fun clearDeleteMessage() {
        _deleteMessage.value = null
    }
}
