package com.example.expensetracker.ui.add

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.expensetracker.databinding.AddExpenseSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

import com.example.expensetracker.model.Expense


class AddExpenseBottomSheet(
    private val onExpenseAdded: (Expense) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: AddExpenseSheetBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AddExpenseSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupDropdown()
        setupDatePicker()

        binding.btnAddExpense.setOnClickListener {

            val category = binding.dropdownCategory.text.toString()
            val description = binding.etDescription.text.toString()
            val date = binding.tvDate.text.toString()
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0

            Log.d("DEBUG_ADD", "Adding Expense: $category | $description | $date | $amount")

            if (category.isEmpty() || description.isEmpty() || date == "Select Date") {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                id = System.currentTimeMillis().toString(),
                category = category,
                description = description,
                date = date,
                amount = amount
            )

            onExpenseAdded(expense)   // send data to HomeActivity
            dismiss()
        }

    }

    private fun setupDropdown() {
        val items = listOf("Food", "Travel", "Shopping", "Others")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, items)
        binding.dropdownCategory.setAdapter(adapter)
        binding.dropdownCategory.keyListener = null
    }

    private fun setupDatePicker() {
        binding.dateContainer.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d -> val monthName = getMonthName(m)
                    binding.tvDate.text = "$d-$monthName-$y"
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun getMonthName(m: Int): String {
        val months = listOf(
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        )
        return months[m]
    }

}
