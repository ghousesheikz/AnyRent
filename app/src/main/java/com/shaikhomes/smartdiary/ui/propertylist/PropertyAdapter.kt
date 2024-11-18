package com.shaikhomes.smartdiary.ui.propertylist

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList

class PropertyAdapter(
    private val context: Context,
    private val leadsList: ArrayList<PropertyList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<PropertyAdapter.LeadViewHolder>() {
    private var leadDataList: List<LeadsList> = emptyList<LeadsList>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PropertyAdapter.LeadViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.property_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var leadClickListener: ((PropertyList) -> Unit)? = null
    private var assignToClickListener: ((PropertyList) -> Unit)? = null
    fun setLeadClickListener(leadList: (PropertyList) -> Unit) {
        this.leadClickListener = leadList
    }

    fun setAssignToClickListener(leadList: (PropertyList) -> Unit) {
        this.assignToClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.propertyName.setText(leadsList[position].project)
        holder.propertyType.setText(leadsList[position].typeoflead)
        holder.propertyType1.setText(leadsList[position].propertytype)
        holder.subPropertyType.setText(leadsList[position].subpropertytype)
        holder.noOfbedrooms.setText(leadsList[position].noofbedrooms)
        holder.location.setText(leadsList[position].locations)
        holder.minAmount.setText("min Amt ₹${leadsList[position].minamount}")
        holder.maxAmount.setText("max Amt ₹${leadsList[position].maxamount}")
        if (isAdmin == true) {
            holder.assignTo.visibility = View.VISIBLE
        } else {
            holder.assignTo.visibility = View.GONE
        }
        if (!leadsList[position].assignto.isNullOrEmpty()) {
            holder.assignTo.text = leadsList[position].assignto
            holder.assignTo.setTextColor(Color.GREEN)
        } else {
            holder.assignTo.setTextColor(Color.BLACK)
            holder.assignTo.setText(context.getText(R.string.add_assignee))
        }
        holder.itemView.setOnClickListener {
            val empData = leadsList[position]
            leadClickListener?.invoke(empData)
        }
        holder.assignTo.setOnClickListener {
            assignToClickListener?.invoke(leadsList[position])
        }
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<PropertyList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<PropertyList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var propertyName: AppCompatTextView
        var propertyType: AppCompatTextView
        var propertyType1: AppCompatTextView
        var subPropertyType: AppCompatTextView
        var noOfbedrooms: AppCompatTextView
        var location: AppCompatTextView
        var minAmount: AppCompatTextView
        var maxAmount: AppCompatTextView
        var assignTo: AppCompatTextView


        init {
            propertyName = itemView.findViewById<AppCompatTextView>(R.id.propertyName)
            propertyType = itemView.findViewById<AppCompatTextView>(R.id.propertyType)
            propertyType1 = itemView.findViewById<AppCompatTextView>(R.id.propertyType1)
            subPropertyType = itemView.findViewById<AppCompatTextView>(R.id.subPropertyType)
            noOfbedrooms = itemView.findViewById<AppCompatTextView>(R.id.noOfbedrooms)
            location = itemView.findViewById<AppCompatTextView>(R.id.location)
            minAmount = itemView.findViewById<AppCompatTextView>(R.id.minAmount)
            maxAmount = itemView.findViewById<AppCompatTextView>(R.id.maxAmount)
            assignTo = itemView.findViewById(R.id.txtAssignTo)

        }
    }

}