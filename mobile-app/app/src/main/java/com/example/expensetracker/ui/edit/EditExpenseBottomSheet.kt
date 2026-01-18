package com.example.expensetracker.ui.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.expensetracker.databinding.EditExpenseSheetBinding
import com.example.expensetracker.model.Expense
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar


class EditExpenseBottomSheet(
    private val expense: Expense,
    private val onExpenseUpdated: (Expense) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: EditExpenseSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EditExpenseSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupDropdown()
        setupFields()
        setupDatePicker()

        binding.btnAddExpense.text = "Save Changes"

        binding.btnAddExpense.setOnClickListener {

            val category = binding.dropdownCategory.text.toString()
            val description = binding.etDescription.text.toString()
            val date = binding.tvDate.text.toString()
            val amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0

            if (category.isEmpty() || description.isEmpty() || date == "Select Date") {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedExpense = Expense(
                id = expense.id,        // SAME ID
                category = category,
                description = description,
                date = date,
                amount = amount
            )

            onExpenseUpdated(updatedExpense)   // 🔥 send to HomeActivity
            dismiss()
        }
    }

    private fun setupFields() {
        binding.dropdownCategory.setText(expense.category)
        binding.etDescription.setText(expense.description)
        binding.tvDate.text = expense.date
        binding.etAmount.setText(expense.amount.toString())
    }

    private fun setupDropdown() {
        val items = listOf("Food", "Travel", "Shopping", "Others")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        binding.dropdownCategory.setAdapter(adapter)
        binding.dropdownCategory.keyListener = null
    }

    private fun setupDatePicker() {
        binding.dateContainer.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val months = listOf(
                        "January","February","March","April","May","June",
                        "July","August","September","October","November","December"
                    )
                    val monthName = months[m]
                    binding.tvDate.text = "$d-$monthName-$y"
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}
