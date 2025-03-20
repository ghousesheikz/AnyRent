package com.shaikhomes.anyrent.ui.availability

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.ui.models.AvailabilityList

class AvailabilityAdapter(
    private val context: Context,
    private val leadsList: ArrayList<AvailabilityList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<AvailabilityAdapter.LeadViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AvailabilityAdapter.LeadViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.availability_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var leadClickListener: ((AvailabilityList) -> Unit)? = null

    fun setLeadClickListener(leadList: (AvailabilityList) -> Unit) {
        this.leadClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.apartmentName.setText(
            Html.fromHtml("Name: <font color='#01335C'>${leadsList[position].apartmentname}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.apartmentFor.setText(
            Html.fromHtml("Address: <font color='#01335C'>${leadsList[position].apartmentfor}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.flatNo.setText(
            Html.fromHtml("Flat No: <font color='#01335C'>${leadsList[position].flat}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.room1.isChecked = leadsList[position].room1 == "Available"
        holder.room2.isChecked = leadsList[position].room2 == "Available"
        holder.room3.isChecked = leadsList[position].room3 == "Available"
        holder.room4.isChecked = leadsList[position].room4 == "Available"
        if (holder.room1.isChecked) {
            holder.room1.setTextColor(context.resources.getColor(R.color.c_white_1))
        } else holder.room1.setTextColor(context.resources.getColor(R.color.black))
        if (holder.room2.isChecked) {
            holder.room2.setTextColor(context.resources.getColor(R.color.c_white_1))
        } else holder.room2.setTextColor(context.resources.getColor(R.color.black))
        if (holder.room3.isChecked) {
            holder.room3.setTextColor(context.resources.getColor(R.color.c_white_1))
        } else holder.room3.setTextColor(context.resources.getColor(R.color.black))
        if (holder.room4.isChecked) {
            holder.room4.setTextColor(context.resources.getColor(R.color.c_white_1))
        } else holder.room4.setTextColor(context.resources.getColor(R.color.black))
        holder.itemView.setOnClickListener {
            val empData = leadsList[position]
            leadClickListener?.invoke(empData)
        }

    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<AvailabilityList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<AvailabilityList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var apartmentName: AppCompatTextView
        var apartmentFor: AppCompatTextView
        var flatNo: AppCompatTextView
        var room1: ToggleButton
        var room2: ToggleButton
        var room3: ToggleButton
        var room4: ToggleButton


        init {
            apartmentName = itemView.findViewById<AppCompatTextView>(R.id.apartmentName)
            apartmentFor = itemView.findViewById<AppCompatTextView>(R.id.apartmentFor)
            flatNo = itemView.findViewById<AppCompatTextView>(R.id.flatNo)
            room1 = itemView.findViewById<ToggleButton>(R.id.room1Toggle)
            room2 = itemView.findViewById<ToggleButton>(R.id.room2Toggle)
            room3 = itemView.findViewById<ToggleButton>(R.id.room3Toggle)
            room4 = itemView.findViewById<ToggleButton>(R.id.room4Toggle)

        }
    }
}