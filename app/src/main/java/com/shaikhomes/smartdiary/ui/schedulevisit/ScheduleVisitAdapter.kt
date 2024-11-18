package com.shaikhomes.smartdiary.ui.schedulevisit

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.models.LeadscheduleList

class ScheduleVisitAdapter(
    private val context: Context,
    private val leadsList: ArrayList<LeadscheduleList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<ScheduleVisitAdapter.LeadViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScheduleVisitAdapter.LeadViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.leadschedule_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var leadClickListener: ((LeadscheduleList) -> Unit)? = null
    private var requirementClickListener: ((LeadscheduleList) -> Unit)? = null
    private var priorityClickListener: ((LeadscheduleList) -> Unit)? = null
    private var assignToClickListener: ((LeadscheduleList) -> Unit)? = null

    fun setLeadClickListener(leadList: (LeadscheduleList) -> Unit) {
        this.leadClickListener = leadList
    }

    fun setRequirementClickListener(leadList: (LeadscheduleList) -> Unit) {
        this.requirementClickListener = leadList
    }

    fun setAssignToClickListener(leadList: (LeadscheduleList) -> Unit) {
        this.assignToClickListener = leadList
    }

    fun setPriorityClickListener(priority: (LeadscheduleList) -> Unit) {
        this.priorityClickListener = priority
    }

    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.leadName.text = if(leadsList[position].leadsname.isNullOrEmpty()) "No Name" else leadsList[position].leadsname
        holder.leadnumber.text = "Number : ${leadsList[position].contactnumber}"
        holder.dateTime.text = "Schedule On : ${leadsList[position].scheduledon}"

        if (isAdmin == true) {
            holder.createdBy.text = "Source: ${leadsList[position].createdby}"
            holder.assignTo.visibility = View.VISIBLE
            holder.createdBy.visibility = View.VISIBLE
        } else {
            holder.createdBy.visibility = View.GONE
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
            leadClickListener?.invoke(leadsList[position])
        }
        holder.assignTo.setOnClickListener {
            assignToClickListener?.invoke(leadsList[position])
        }
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<LeadscheduleList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<LeadscheduleList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leadName: AppCompatTextView
        var leadnumber: AppCompatTextView
        var createdBy: AppCompatTextView
        var assignTo: AppCompatTextView
        var dateTime: AppCompatTextView

        init {
            leadName = itemView.findViewById<AppCompatTextView>(R.id.leadName)
            leadnumber = itemView.findViewById<AppCompatTextView>(R.id.leadnumber)
            dateTime = itemView.findViewById<AppCompatTextView>(R.id.dateTime)
            createdBy = itemView.findViewById(R.id.createdBy)
            assignTo = itemView.findViewById(R.id.txtAssignTo)
        }
    }

}