package com.example.expensetracker.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.databinding.ItemExpenseBinding
import com.example.expensetracker.model.Expense

class ExpenseAdapter(
    val onEdit: (Expense) -> Unit,
    val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(Diff()) {

    inner class ExpenseViewHolder(val binding: ItemExpenseBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(exp: Expense) {
            binding.tvDescription.text = exp.description
            binding.tvDate.text = exp.date
            binding.tvPrice.text = "₹${exp.amount}"

            binding.colorStrip.setBackgroundColor(
                when (exp.category) {
                    "Food" -> Color.parseColor("#FFB84D")
                    "Travel" -> Color.parseColor("#0099FF")
                    "Shopping" -> Color.parseColor("#7B61FF")
                    else -> Color.parseColor("#009966")
                }
            )

            binding.btnEdit.setOnClickListener { onEdit(exp) }
            binding.btnDelete.setOnClickListener { onDelete(exp) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class Diff : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(o: Expense, n: Expense) = o.id == n.id
        override fun areContentsTheSame(o: Expense, n: Expense) = o == n
    }
}
