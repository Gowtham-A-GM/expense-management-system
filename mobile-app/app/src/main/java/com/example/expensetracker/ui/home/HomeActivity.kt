package com.example.expensetracker.ui.home

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityHomeBinding
import com.example.expensetracker.model.Expense
import com.example.expensetracker.network.NetworkLiveData
import com.example.expensetracker.ui.add.AddExpenseBottomSheet
import com.example.expensetracker.ui.edit.EditExpenseBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()
    private lateinit var uid: String


    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NetworkLiveData(this).observe(this) { isConnected ->
            if (!isConnected) {
                showNoInternetDialog()
            }
        }

        uid = getSharedPreferences("USER", MODE_PRIVATE)
            .getString("uid", "")!!

        loadProfileName()

        setTodayDateInUI()

        vm.loadExpenses(uid)

        setupLogout()
        setupRecycler()
        setupSwipeToDelete()
        setupDateSelector()
        setupCategoryChips()
        setupAddButton()
        observeLiveData()
    }

    private fun showNoInternetDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Internet is required to sync your expenses. Please check your network.")
            .setCancelable(true)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun hasInternet(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val cap = cm.getNetworkCapabilities(network) ?: return false
        return cap.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupLogout() {
        binding.imgAvatar.setOnClickListener {

            // Clear shared preferences
            val prefs = getSharedPreferences("USER", MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Firebase logout
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // Google SignOut
            com.google.android.gms.auth.api.signin.GoogleSignIn
                .getClient(
                    this,
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                    ).build()
                ).signOut()

            // Navigate to Login
            val intent = Intent(this, com.example.expensetracker.ui.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun loadProfileName() {

        val prefs = getSharedPreferences("USER", MODE_PRIVATE)
        var profileName = prefs.getString("profile_name", null)

        if (profileName == null) {

            val uid = prefs.getString("uid", null) ?: return

            FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener { snap ->
                    profileName = snap.child("profileName").value.toString()

                    // Save locally again
                    prefs.edit().putString("profile_name", profileName).apply()

                    binding.tvGreeting.text = "Hi $profileName"
                }
        } else {
            binding.tvGreeting.text = "Hi $profileName"
        }
    }


    private fun setupRecycler() {
        adapter = ExpenseAdapter(
            onEdit = { expense ->
                if (!hasInternet()) {
                    showNoInternetDialog()
                    return@ExpenseAdapter
                }
                EditExpenseBottomSheet(expense) { updatedExpense ->
                    vm.updateExpense(updatedExpense, uid)
                }.show(supportFragmentManager, "editSheet")
            },
            onDelete = { expense ->
                if (!hasInternet()) {
                    showNoInternetDialog()
                    return@ExpenseAdapter
                }
                vm.deleteExpense(expense, uid)
            }

        )

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)   // 🔥 FIX
        binding.rvExpenses.adapter = adapter
    }


    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (!hasInternet()) {
                    showNoInternetDialog()
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                    return
                }

                val position = viewHolder.adapterPosition
                val expense = adapter.currentList[position]
                vm.deleteExpense(expense, uid)     // DELETE FROM VIEWMODEL
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvExpenses)
    }


    private fun setTodayDateInUI() {
        val c = Calendar.getInstance()

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dayName = getDayName(year, month, day)
        val monthName = getMonthName(month)

        val formatted = "$day, $dayName, $monthName, $year"

        binding.tvMonth.text = formatted

        // Also update ViewModel filter:
        vm.changeDate("$day-$monthName-$year")
    }


    private fun setupDateSelector() {
        binding.tvMonth.setOnClickListener { openDatePicker() }
    }

    private fun openDatePicker() {
        val c = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val dayName = getDayName(year, month, day)
                val monthName = getMonthName(month)

                val formatted = "$day, $dayName, $monthName, $year"

                binding.tvMonth.text = formatted

                // store date in consistent format for filtering
                vm.changeDate("$day-$monthName-$year")
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getMonthName(m: Int): String {
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return months[m]
    }

    private fun getDayName(year: Int, month: Int, day: Int): String {
        val c = Calendar.getInstance()
        c.set(year, month, day)

        return when (c.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Sunday"
        }
    }


    private fun setupCategoryChips() {
        binding.chipAll.setOnClickListener { vm.changeCategory("All") }
        binding.chipFood.setOnClickListener { vm.changeCategory("Food") }
        binding.chipTravel.setOnClickListener { vm.changeCategory("Travel") }
        binding.chipShopping.setOnClickListener { vm.changeCategory("Shopping") }
        binding.chipOthers.setOnClickListener { vm.changeCategory("Others") }
    }

    private fun setupAddButton() {
        binding.fabAdd.setOnClickListener {
            if (!hasInternet()) {
                showNoInternetDialog()
                return@setOnClickListener
            }
            AddExpenseBottomSheet { newExpense ->
                vm.addExpense(newExpense, uid)     // ADD TO LIST in ViewModel
            }.show(supportFragmentManager, "addSheet")
        }
    }


    private fun observeLiveData() {
        vm.expensesList.observe(this) { list ->
            Log.d("DEBUG_UI", "Recycler Received list size = ${list.size}")
            adapter.submitList(list)
        }

        vm.selectedCategory.observe(this) { cat ->
            highlightCategory(cat)
        }

        vm.foodTotal.observe(this) { total ->
            binding.tvFoodMoney.text = "₹$total"
        }

        vm.travelTotal.observe(this) { total ->
            binding.tvTravelMoney.text = "₹$total"
        }

        vm.shoppingTotal.observe(this) { total ->
            binding.tvShoppingMoney.text = "₹$total"
        }

        vm.othersTotal.observe(this) { total ->
            binding.tvOthersMoney.text = "₹$total"
        }

        vm.overallTotal.observe(this) { total ->
            binding.tvTotalMoney.text = "₹$total"
        }

    }

    private fun highlightCategory(cat: String) {
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.bg_chip_selected)
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected)

        val selectedTextColor = ContextCompat.getColor(this, R.color.white)
        val unselectedTextColor = ContextCompat.getColor(this, R.color.dark_violet)

        // All
        binding.chipAll.apply {
            background = if (cat == "All") selectedBg else unselectedBg
            setTextColor(if (cat == "All") selectedTextColor else unselectedTextColor)
        }

        // Food
        binding.chipFood.apply {
            background = if (cat == "Food") selectedBg else unselectedBg
            setTextColor(if (cat == "Food") selectedTextColor else unselectedTextColor)
        }

        // Travel
        binding.chipTravel.apply {
            background = if (cat == "Travel") selectedBg else unselectedBg
            setTextColor(if (cat == "Travel") selectedTextColor else unselectedTextColor)
        }

        // Shopping
        binding.chipShopping.apply {
            background = if (cat == "Shopping") selectedBg else unselectedBg
            setTextColor(if (cat == "Shopping") selectedTextColor else unselectedTextColor)
        }

        // Others
        binding.chipOthers.apply {
            background = if (cat == "Others") selectedBg else unselectedBg
            setTextColor(if (cat == "Others") selectedTextColor else unselectedTextColor)
        }

        vm.deleteMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearDeleteMessage()
            }
        }

    }

}
