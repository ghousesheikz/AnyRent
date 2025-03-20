package com.shaikhomes.anyrent

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kevinschildhorn.otpview.OTPView
import com.shaikhomes.anyrent.databinding.ActivityExpensesListBinding
import com.shaikhomes.anyrent.ui.adapters.ExpensesAdapter
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.models.ExpensesList
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpensesList : AppCompatActivity() {
    private lateinit var activityExpensesListBinding: ActivityExpensesListBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var expensesAdapter: ExpensesAdapter? = null
    private var filterDate: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityExpensesListBinding = ActivityExpensesListBinding.inflate(layoutInflater)
        setContentView(activityExpensesListBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Expenses"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityExpensesListBinding?.apply {
            expenseList.layoutManager = LinearLayoutManager(this@ExpensesList)
            expensesAdapter = ExpensesAdapter(this@ExpensesList, arrayListOf(), false).apply {
                setDeleteClickListener { expenses->
                    deleteExpenses(expenses)
                }
            }
            expenseList.adapter = expensesAdapter
            todayToggle.isChecked = true
            todayToggle.setTextColor(Color.parseColor("#FFFFFF"))
            filterDate = "today"
            todayToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    filterDate = "today"
                    weekToggle.isChecked = false
                    monthlyToggle.isChecked = false
                    allToggle.isChecked = false
                    creditToggle.isChecked = false
                    debitToggle.isChecked = false
                    debitToggle.setTextColor(Color.parseColor("#000000"))
                    creditToggle.setTextColor(Color.parseColor("#000000"))
                    todayToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    weekToggle.setTextColor(Color.parseColor("#000000"))
                    monthlyToggle.setTextColor(Color.parseColor("#000000"))
                    allToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate)
                    getTotalAmount()
                }
            }
            weekToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    filterDate = "week"
                    todayToggle.isChecked = false
                    monthlyToggle.isChecked = false
                    allToggle.isChecked = false
                    creditToggle.isChecked = false
                    debitToggle.isChecked = false
                    debitToggle.setTextColor(Color.parseColor("#000000"))
                    creditToggle.setTextColor(Color.parseColor("#000000"))
                    weekToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    todayToggle.setTextColor(Color.parseColor("#000000"))
                    monthlyToggle.setTextColor(Color.parseColor("#000000"))
                    allToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate)
                    getTotalAmount()
                }
            }
            monthlyToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    filterDate = "month"
                    todayToggle.isChecked = false
                    weekToggle.isChecked = false
                    allToggle.isChecked = false
                    creditToggle.isChecked = false
                    debitToggle.isChecked = false
                    debitToggle.setTextColor(Color.parseColor("#000000"))
                    creditToggle.setTextColor(Color.parseColor("#000000"))
                    monthlyToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    todayToggle.setTextColor(Color.parseColor("#000000"))
                    weekToggle.setTextColor(Color.parseColor("#000000"))
                    allToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate)
                    getTotalAmount()
                }
            }
            allToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    filterDate = "all"
                    todayToggle.isChecked = false
                    weekToggle.isChecked = false
                    monthlyToggle.isChecked = false
                    creditToggle.isChecked = false
                    debitToggle.isChecked = false
                    debitToggle.setTextColor(Color.parseColor("#000000"))
                    creditToggle.setTextColor(Color.parseColor("#000000"))
                    allToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    todayToggle.setTextColor(Color.parseColor("#000000"))
                    weekToggle.setTextColor(Color.parseColor("#000000"))
                    monthlyToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate)
                    getTotalAmount()
                }
            }
            creditToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    debitToggle.isChecked = false
                    creditToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    debitToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate,"credit")
                    getTotalAmount()
                }
            }
            debitToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    creditToggle.isChecked = false
                    debitToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    creditToggle.setTextColor(Color.parseColor("#000000"))
                    expensesAdapter?.filter(filterDate,"debit")
                    getTotalAmount()
                }
            }
        }
        getExpenses()

    }

    private fun deleteExpenses(expenses: ExpensesList) {
        val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
        val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
        AlertDialog.Builder(this@ExpensesList).apply {
            this.setMessage("Do you want to delete this expense?")
            this.setView(dialogView)
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                if (otpView.getStringFromFields() == "278692") {
                    expenses.update = "delete"
                    addApartmentViewModel?.addExpenses(expenses, success = {
                        if (it.status == "200") {
                            showToast(this@ExpensesList, "Expenses deleted successfully")
                            getExpenses()
                            activityExpensesListBinding.todayToggle.isChecked = true
                            filterDate = "today"
                        }
                    }, error = {
                        showToast(this@ExpensesList, it)
                    })
                } else showToast(this@ExpensesList, "Incorrect OTP")
            }
            this.setNegativeButton(
                "NO"
            ) { p0, p1 ->
                p0.dismiss()
            }
            this.setCancelable(true)
            this.show()
        }
    }

    private fun getExpenses() {
        addApartmentViewModel?.getExpenses(success = { expensesData ->
            CoroutineScope(Dispatchers.Main).launch {
                if (!expensesData.expensesList.isNullOrEmpty()) {
                    expensesAdapter?.updateList(expensesData.expensesList)
                    expensesAdapter?.filter(filterDate)
                    getTotalAmount()
                }
            }
        }, error = {}, apartmentid = prefmanager.selectedApartment?.ID.toString(), "", "")
    }

    private fun getTotalAmount() {
        CoroutineScope(Dispatchers.IO).launch {
            var amount = 0
            expensesAdapter?.getList()?.forEach { records->
                if(!records.creditAmount.isNullOrEmpty()) amount += records.creditAmount?.toInt()!!
                if(!records.debitAmount.isNullOrEmpty()) amount -= records.debitAmount?.toInt()!!
            }
            withContext(Dispatchers.Main){
                activityExpensesListBinding.totalAmount.setText("Total Amount : AED ${amount}/-")
            }
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }
}