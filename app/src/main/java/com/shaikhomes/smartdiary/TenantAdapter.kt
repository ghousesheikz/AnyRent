package com.shaikhomes.smartdiary

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.models.TenantList

class TenantAdapter(
    private val context: Context,
    private val leadsList: ArrayList<TenantList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<TenantAdapter.LeadViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TenantAdapter.LeadViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.tenant_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var editClickListener: ((TenantList) -> Unit)? = null
    private var availableBedsClick: ((TenantList, AppCompatTextView) -> Unit)? = null

    fun setEditClickListener(leadList: (TenantList) -> Unit) {
        this.editClickListener = leadList
    }

    fun setApartmentClickListener(leadList: (TenantList, AppCompatTextView) -> Unit) {
        this.availableBedsClick = leadList
    }

    private var callClickListener: ((TenantList) -> Unit)? = null
    private var deleteClickListener: ((TenantList) -> Unit)? = null

    fun setCallClickListener(leadList: (TenantList) -> Unit) {
        this.callClickListener = leadList
    }

    fun setDeleteClickListener(leadList: (TenantList) -> Unit) {
        this.deleteClickListener = leadList
    }

    private var reminderClickListener: ((TenantList) -> Unit)? = null

    fun setReminderClickListener(leadList: (TenantList) -> Unit) {
        this.reminderClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.tenantName.setText(
            Html.fromHtml("Name: <font color='#000E77'>${leadsList[position].Name}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.rent.text = if(!leadsList[position].rent.isNullOrEmpty()) leadsList[position].rent else "0/-"
        holder.joined.text = leadsList[position].checkout
        holder.tenantLayout.setOnLongClickListener {
            deleteClickListener?.invoke(leadsList[position])
            true
        }
        holder.contact.setOnClickListener {
            callClickListener?.invoke(leadsList[position])
        }
        holder.reminder.setOnClickListener {
            reminderClickListener?.invoke(leadsList[position])
        }
        try {
            if (!leadsList[position].apartmentId.isNullOrEmpty()) {
                availableBedsClick?.invoke(leadsList[position], holder.apartment)
            } else holder.apartment.text = "Not Assigned"
        } catch (exp: Exception) {
            //
        }
    }

    fun getFloorData(data: String?): String {
        val type = object : TypeToken<ArrayList<String>>() {}.type
        val list: ArrayList<String> = Gson().fromJson(data, type)
        var floors: String = ""
        list.forEachIndexed { index, s ->
            floors += if (index == 0) s else ", ${s}"
        }
        return floors
    }

    override fun getItemCount(): Int {
        return leadsList.size
    }

    fun updateList(leadsList: List<TenantList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<TenantList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tenantName: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.tenantName)
        var rent: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.rent)
        var apartment: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.apartment)
        var joined: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.joined)
        var contact: AppCompatButton = itemView.findViewById<AppCompatButton>(R.id.contact)
        var reminder: AppCompatButton = itemView.findViewById<AppCompatButton>(R.id.reminder)
        var tenantLayout: ConstraintLayout = itemView.findViewById<ConstraintLayout>(R.id.tenantLayout)

    }
}