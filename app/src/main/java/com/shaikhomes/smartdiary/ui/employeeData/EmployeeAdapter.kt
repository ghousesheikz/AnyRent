package com.shaikhomes.smartdiary.ui.employeeData

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.ui.home.LeadAdapter
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.dateFormat

class EmployeeAdapter(
    private val context: Context,
    private val leadsList: ArrayList<UserDetailsList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<EmployeeAdapter.LeadViewHolder>() {
    private var leadDataList: List<LeadsList> = emptyList<LeadsList>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeAdapter.LeadViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.employee_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var leadClickListener: ((UserDetailsList) -> Unit)? = null

    fun setLeadClickListener(leadList: (UserDetailsList) -> Unit) {
        this.leadClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.employeeName.setText(Html.fromHtml("Name: <font color='#01335C'>${leadsList[position].UserName}</font>"), TextView.BufferType.SPANNABLE)
        holder.txtAddress.setText(Html.fromHtml("Address: <font color='#01335C'>${leadsList[position].Address}</font>"), TextView.BufferType.SPANNABLE)
        holder.switchStatus.isChecked = leadsList[position].Active == "1"
        holder.itemView.setOnClickListener {
            val empData = leadsList[position]
            leadClickListener?.invoke(empData)
        }
       /* leadDataList.filter { it.assignto == leadsList[position].UserName }.let {
            if(!it.isNullOrEmpty()){
                holder.txtLeadsCount.setText(Html.fromHtml("No. of Leads <font color='red'>${it.size}</font>"), TextView.BufferType.SPANNABLE)

            }else{
                holder.txtLeadsCount.setText(Html.fromHtml("No. of Leads <font color='red'>0</font>"), TextView.BufferType.SPANNABLE)
            }
        }*/
        holder.txtLeadsCount.setText(Html.fromHtml("No. of Leads <font color='red'>${leadsList[position].leadsCount}</font>"), TextView.BufferType.SPANNABLE)
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<UserDetailsList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<UserDetailsList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var employeeName: AppCompatTextView
        var txtLeadsCount: AppCompatTextView
        var txtAddress: AppCompatTextView
        var switchStatus: SwitchCompat


        init {
            employeeName = itemView.findViewById<AppCompatTextView>(R.id.employeeName)
            txtLeadsCount = itemView.findViewById<AppCompatTextView>(R.id.txtLeadsCount)
            txtAddress = itemView.findViewById<AppCompatTextView>(R.id.txtAddress)
            switchStatus = itemView.findViewById<SwitchCompat>(R.id.switchStatus)

        }
    }

}