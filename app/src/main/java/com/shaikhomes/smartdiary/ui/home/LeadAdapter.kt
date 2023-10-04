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
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList
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

    private var leadClickListener: ((LeadsList, Int) -> Unit)? = null
    private var requirementClickListener: ((LeadsList) -> Unit)? = null
    private var priorityClickListener: ((LeadsList) -> Unit)? = null
    private var assignToClickListener: ((LeadsList) -> Unit)? = null
    private var statusClickListener: ((LeadsList) -> Unit)? = null

    fun setLeadClickListener(leadList: (LeadsList, Int) -> Unit) {
        this.leadClickListener = leadList
    }

    fun setRequirementClickListener(leadList: (LeadsList) -> Unit) {
        this.requirementClickListener = leadList
    }

    fun setAssignToClickListener(leadList: (LeadsList) -> Unit) {
        this.assignToClickListener = leadList
    }

    fun setStatusClickListener(leadList: (LeadsList) -> Unit) {
        this.statusClickListener = leadList
    }

    fun setPriorityClickListener(priority: (LeadsList) -> Unit) {
        this.priorityClickListener = priority
    }

    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.leadName.text =
            leadsList[position].leadsname.plus(
                " (${
                    (if (!leadsList[position].countrycode.isNullOrEmpty()) leadsList[position].countrycode else "").plus(
                        leadsList[position].contactnumber
                    )
                })"
            )
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
            val lookingfor = if (leadsList[position].lookingfor?.toLowerCase() == "male") {
                Html.fromHtml("Source: ${leadsList[position].createdby}    LookingFor: <font color='#0a45d0'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "female") {
                Html.fromHtml("Source: ${leadsList[position].createdby}    LookingFor: <font color='#e75480'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "family") {
                Html.fromHtml("Source: ${leadsList[position].createdby}    LookingFor: <font color='#644117'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "couples") {
                Html.fromHtml("Source: ${leadsList[position].createdby}    LookingFor: <font color='#FF0000'>${leadsList[position].lookingfor}</font>")
            } else ""
            holder.createdBy.setText(lookingfor, TextView.BufferType.SPANNABLE)
            holder.assignTo.visibility = View.VISIBLE
            holder.createdBy.visibility = View.VISIBLE
        } else {
            val lookingfor = if (leadsList[position].lookingfor?.toLowerCase() == "male") {
                Html.fromHtml("LookingFor: <font color='#0a45d0'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "female") {
                Html.fromHtml("LookingFor: <font color='#e75480'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "family") {
                Html.fromHtml("LookingFor: <font color='#644117'>${leadsList[position].lookingfor}</font>")
            } else if (leadsList[position].lookingfor?.toLowerCase() == "couples") {
                Html.fromHtml("LookingFor: <font color='#FF0000'>${leadsList[position].lookingfor}</font>")
            } else ""
            holder.createdBy.setText(lookingfor, TextView.BufferType.SPANNABLE)
            holder.assignTo.visibility = View.GONE
        }
        if (!leadsList[position].assignto.isNullOrEmpty()) {
            holder.assignTo.text = leadsList[position].assignto
            holder.assignTo.setTextColor(Color.GREEN)
        } else {
            holder.assignTo.setTextColor(Color.BLACK)
            holder.assignTo.setText(context.getText(R.string.add_assignee))
        }
        assignStatus(holder.status, leadsList[position].status, context)
        when (leadsList[position].status) {
            "Interested" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.interested_border))
            }

            "Not Interested" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.not_interested_border))
            }

            "Low Budget" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.low_budget_border))
            }

            "Junk Lead" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.junk_lead_border))
            }

            "Confirmed" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.confirmed_border))
            }

            "Site Visit Done" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.site_visit_done_border))
            }

            null -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.layout_border))
            }

            "" -> {
                holder.status.setBackgroundDrawable(context.resources.getDrawable(R.drawable.layout_border))
            }
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
            leadClickListener?.invoke(leadsList[position], position)
        }
        holder.assignTo.setOnClickListener {
            assignToClickListener?.invoke(leadsList[position])
        }
        holder.status.setOnClickListener {
            statusClickListener?.invoke(leadsList[position])
        }
        holder.addRequirement.setOnClickListener {
            requirementClickListener?.invoke(leadsList[position])
        }
        if (!leadsList[position].feedback.isNullOrEmpty()) {
            holder.feedback.apply {
                this.visibility = View.VISIBLE
                this.text = "Feedback : ${leadsList[position].feedback}"
            }
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
                    } else {
                        holder.propertyData.visibility = View.GONE
                    }
                }
        }
    }

    private fun assignStatus(txtStatus: AppCompatTextView, status: String?, context: Context) {
        if (status.isNullOrEmpty()) {
            txtStatus.setHint(context.getString(R.string.add_status))
            txtStatus.setText("")
        } else {
            txtStatus.setText(status)
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
        var cardView: CardView
        var createdBy: AppCompatTextView
        var assignTo: AppCompatTextView
        var status: AppCompatTextView
        var feedback: AppCompatTextView
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
            cardView = itemView.findViewById(R.id.cardView)
            createdBy = itemView.findViewById(R.id.createdBy)
            highToggle = itemView.findViewById(R.id.highToggle)
            mediumToggle = itemView.findViewById(R.id.mediumToggle)
            lowToggle = itemView.findViewById(R.id.lowToggle)
            priorityGroup = itemView.findViewById(R.id.priorityGroup)
            assignTo = itemView.findViewById(R.id.txtAssignTo)
            status = itemView.findViewById(R.id.txtStatus)
            feedback = itemView.findViewById(R.id.feedback)
            addRequirement = itemView.findViewById(R.id.addRequirement)
        }
    }

}