package com.shaikhomes.anyrent.ui.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.CircleTransformation
import com.shaikhomes.anyrent.ui.models.ExpensesList
import com.shaikhomes.anyrent.ui.utils.dateFormat
import com.shaikhomes.anyrent.ui.utils.getExpensesList

class ExpensesAdapter(
    private val context: Context,
    private val leadsList: ArrayList<ExpensesList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<ExpensesAdapter.LeadViewHolder>() {
    private var filteredList: MutableList<ExpensesList> = leadsList.toMutableList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpensesAdapter.LeadViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.expenses_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var editClickListener: ((ExpensesList) -> Unit)? = null
    private var availableBedsClick: ((ExpensesList, AppCompatTextView) -> Unit)? = null

    fun setEditClickListener(leadList: (ExpensesList) -> Unit) {
        this.editClickListener = leadList
    }

    fun setApartmentClickListener(leadList: (ExpensesList, AppCompatTextView) -> Unit) {
        this.availableBedsClick = leadList
    }

    private var callClickListener: ((ExpensesList) -> Unit)? = null
    private var deleteClickListener: ((ExpensesList) -> Unit)? = null

    fun setCallClickListener(leadList: (ExpensesList) -> Unit) {
        this.callClickListener = leadList
    }

    fun setDeleteClickListener(leadList: (ExpensesList) -> Unit) {
        this.deleteClickListener = leadList
    }

    private var reminderClickListener: ((ExpensesList) -> Unit)? = null

    fun setReminderClickListener(leadList: (ExpensesList) -> Unit) {
        this.reminderClickListener = leadList
    }

    fun filter(query: String?, creditdebit: String? = null) {
        if (query == null || query == "all") {
            filteredList = if (!creditdebit.isNullOrEmpty()) {
                if (creditdebit == "debit") {
                    leadsList.filter { it.debitAmount != "0" }.toMutableList()
                } else leadsList.filter { it.debitAmount == "0" }.toMutableList()
            } else leadsList.toMutableList()
            notifyDataSetChanged()
        } else {
            filteredList = if (query == "today") {
                val expenseList = getExpensesList(leadsList, currentDay = true).toMutableList()
                if (!creditdebit.isNullOrEmpty()) {
                    if (creditdebit == "debit") {
                        expenseList.filter { it.debitAmount != "0" }.toMutableList()
                    } else expenseList.filter { it.debitAmount == "0" }.toMutableList()
                } else expenseList
            } else if (query == "week") {
                val expenseList = getExpensesList(leadsList, currentWeek = true).toMutableList()
                if (!creditdebit.isNullOrEmpty()) {
                    if (creditdebit == "debit") {
                        expenseList.filter { it.debitAmount != "0" }.toMutableList()
                    } else expenseList.filter { it.debitAmount == "0" }.toMutableList()
                } else expenseList
            } else if (query == "month") {
                val expenseList = getExpensesList(leadsList, currentMonth = true).toMutableList()
                if (!creditdebit.isNullOrEmpty()) {
                    if (creditdebit == "debit") {
                        expenseList.filter { it.debitAmount != "0" }.toMutableList()
                    } else expenseList.filter { it.debitAmount == "0" }.toMutableList()
                } else expenseList
            } else leadsList.toMutableList()
            notifyDataSetChanged()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        val item = filteredList[position]
        holder.amount.text =
            "AED ${if (!item.debitAmount.isNullOrEmpty() && item.debitAmount != "0") item.debitAmount else item.creditAmount}/-"
        holder.description.text = item.notes
        holder.paymentMode.text = "Payment mode : ${item.paymentMode}"
        holder.paidOn.text = item.receivedOn?.dateFormat("dd-MM-yyyy hh:mm:ss", "dd-MMM-yyyy")
        if (!item.picture.isNullOrEmpty()) {
            Glide.with(context)
                .load(item.picture)
                .transform(CircleTransformation()) // Apply custom circle transformation
                .into(holder.circularImageView)
        }
        if (!item.debitAmount.isNullOrEmpty() && item.debitAmount != "0") {
            Glide.with(context)
                .load(R.drawable.ic_debit)
                .transform(CircleTransformation()) // Apply custom circle transformation
                .into(holder.creditDebit)
        } else {
            Glide.with(context)
                .load(R.drawable.ic_credit)
                .transform(CircleTransformation()) // Apply custom circle transformation
                .into(holder.creditDebit)
        }
        holder.itemView.rootView.setOnLongClickListener {
            deleteClickListener?.invoke(item)
            true
        }
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun updateList(leadsList: List<ExpensesList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
        }
        this.filteredList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
        }
        notifyDataSetChanged()
    }

    fun getList(): MutableList<ExpensesList> {
        return filteredList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var amount: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.amount)
        var description: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.description)
        var paidOn: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.paidOn)
        var circularImageView: ImageView = itemView.findViewById<ImageView>(R.id.circularImageView)
        var creditDebit: ImageView = itemView.findViewById<ImageView>(R.id.creditDebit)
        var paymentMode: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.paymentMode)

    }
}