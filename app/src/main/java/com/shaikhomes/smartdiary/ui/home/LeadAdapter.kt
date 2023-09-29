package com.shaikhomes.smartdiary.ui.home

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
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.dateFormat


class LeadAdapter(
    private val context: Context,
    private val leadsList: ArrayList<LeadsList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<LeadAdapter.LeadViewHolder>() {
    private var propertyDataList: List<PropertyList> = emptyList<PropertyList>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeadAdapter.LeadViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.lead_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var leadClickListener: ((LeadsList) -> Unit)? = null
    private var requirementClickListener: ((LeadsList) -> Unit)? = null
    private var priorityClickListener: ((LeadsList) -> Unit)? = null
    private var assignToClickListener: ((LeadsList) -> Unit)? = null

    fun setLeadClickListener(leadList: (LeadsList) -> Unit) {
        this.leadClickListener = leadList
    }

    fun setRequirementClickListener(leadList: (LeadsList) -> Unit) {
        this.requirementClickListener = leadList
    }

    fun setAssignToClickListener(leadList: (LeadsList) -> Unit) {
        this.assignToClickListener = leadList
    }

    fun setPriorityClickListener(priority: (LeadsList) -> Unit) {
        this.priorityClickListener = priority
    }

    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.leadName.text =
            leadsList[position].leadsname.plus(" (${(if (!leadsList[position].countrycode.isNullOrEmpty()) leadsList[position].countrycode else "").plus(leadsList[position].contactnumber)})")
        holder.dateTime.text =
            if (!leadsList[position].updatedon.isNullOrEmpty()) leadsList[position].updatedon?.dateFormat(
                "dd-MM-yyyy HH:mm:ss",
                "dd MMM yyyy HH:mm:ss"
            )
            else leadsList[position].date?.dateFormat("dd-MM-yyyy HH:mm:ss", "dd MMM yyyy")

        holder.callLead.setOnClickListener {
            try {
                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + leadsList[position].contactnumber)
                )
                context.startActivity(intent)
            } catch (exp: Exception) {
                exp.printStackTrace()
            }
        }
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
        if (leadsList[position].priority.isNullOrEmpty()) {
            holder.priorityGroup.visibility = View.VISIBLE
        } else {
            holder.priorityGroup.visibility = View.GONE
            val dateTime = when (leadsList[position].priority) {
                "High" -> {
                    "${holder.dateTime.text.toString()} <font color='red'>${leadsList[position].priority?.toUpperCase()}</font>"
                }

                "Medium" -> {
                    "${holder.dateTime.text.toString()} <font color='#FFA500'>${leadsList[position].priority?.toUpperCase()}</font>"
                }

                "Low" -> {
                    "${holder.dateTime.text.toString()} <font color='green'>${leadsList[position].priority?.toUpperCase()}</font>"
                }

                else -> holder.dateTime.text.toString()
            }
            holder.dateTime.setText(Html.fromHtml(dateTime), TextView.BufferType.SPANNABLE)
        }
        holder.highToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList[position].priority = "High"
                priorityClickListener?.invoke(leadsList[position])
                holder.highToggle.setTextColor(context.resources.getColor(R.color.c_white_1))
                holder.lowToggle.isChecked = false
                holder.mediumToggle.isChecked = false
            } else {
                holder.highToggle.setTextColor(context.resources.getColor(R.color.c_black_1))
            }
        }

        holder.mediumToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList[position].priority = "Medium"
                priorityClickListener?.invoke(leadsList[position])
                holder.mediumToggle.setTextColor(context.resources.getColor(R.color.c_white_1))
                holder.lowToggle.isChecked = false
                holder.highToggle.isChecked = false
            } else {
                holder.mediumToggle.setTextColor(context.resources.getColor(R.color.c_black_1))
            }
        }
        holder.lowToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList[position].priority = "Low"
                priorityClickListener?.invoke(leadsList[position])
                holder.highToggle.isChecked = false
                holder.mediumToggle.isChecked = false
                holder.lowToggle.setTextColor(context.resources.getColor(R.color.c_white_1))
            } else {
                holder.lowToggle.setTextColor(context.resources.getColor(R.color.c_black_1))
            }
        }
        holder.itemView.setOnClickListener {
            leadClickListener?.invoke(leadsList[position])
        }
        holder.assignTo.setOnClickListener {
            assignToClickListener?.invoke(leadsList[position])
        }
        holder.addRequirement.setOnClickListener {
            requirementClickListener?.invoke(leadsList[position])
        }
        if (propertyDataList.isNotEmpty()) {
            propertyDataList.filter { it.contactnumber == leadsList[position].contactnumber }
                .let { propertyData ->
                    if (propertyData.isNotEmpty()) {
                        holder.propertyData.apply {
                            this.visibility = View.VISIBLE
                            val data = propertyData.first()
                            this.text =
                                "For ${data.typeoflead} ${data.subpropertytype} in ${data.locations}. \nBudget ₹${data.minamount} - ₹${data.maxamount}"
                        }
                    }else{
                        holder.propertyData.visibility = View.GONE
                    }
                }
        }
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<LeadsList>, propertyList: List<PropertyList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            propertyDataList = propertyList
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<LeadsList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var leadName: AppCompatTextView
        var dateTime: AppCompatTextView
        var propertyData: AppCompatTextView
        var callLead: AppCompatImageView
        var createdBy: AppCompatTextView
        var assignTo: AppCompatTextView
        var addRequirement: AppCompatTextView
        var highToggle: ToggleButton
        var mediumToggle: ToggleButton
        var lowToggle: ToggleButton
        var priorityGroup: Group

        init {
            leadName = itemView.findViewById<AppCompatTextView>(R.id.leadName)
            dateTime = itemView.findViewById<AppCompatTextView>(R.id.dateTime)
            propertyData = itemView.findViewById<AppCompatTextView>(R.id.propertyData)
            callLead = itemView.findViewById(R.id.Imgcall)
            createdBy = itemView.findViewById(R.id.createdBy)
            highToggle = itemView.findViewById(R.id.highToggle)
            mediumToggle = itemView.findViewById(R.id.mediumToggle)
            lowToggle = itemView.findViewById(R.id.lowToggle)
            priorityGroup = itemView.findViewById(R.id.priorityGroup)
            assignTo = itemView.findViewById(R.id.txtAssignTo)
            addRequirement = itemView.findViewById(R.id.addRequirement)
        }
    }

}