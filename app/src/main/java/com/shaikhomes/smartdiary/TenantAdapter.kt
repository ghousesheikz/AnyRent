package com.shaikhomes.smartdiary

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.calculateCharge
import com.shaikhomes.smartdiary.ui.utils.calculateDaysBetween
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
import com.shaikhomes.smartdiary.ui.utils.makeCamelCase
import java.lang.Math.abs

class TenantAdapter(
    private val context: Context,
    private val leadsList: ArrayList<TenantList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<TenantAdapter.LeadViewHolder>() {
    private var filteredList: MutableList<TenantList> = leadsList.toMutableList()

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

    fun filter(query: String?) {
        if (query == null) {
            filteredList = leadsList.toMutableList()
            notifyDataSetChanged()
        } else {
            filteredList = leadsList.filter {
                it.Name?.contains(query, ignoreCase = true) == true || it.details?.contains(
                    query,
                    ignoreCase = true
                ) == true ||
                        it.MobileNo?.removePrefix("+")
                            .let { it?.contains(query, ignoreCase = true) == true }
            }.toMutableList()
            notifyDataSetChanged()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.tenantName.setText(
            Html.fromHtml("Name: <font color='#000E77'>${filteredList[position].Name}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.joined.setText(
            Html.fromHtml(
                "CheckIn: <font color='#000E77'>${
                    filteredList[position].checkin?.dateFormat(
                        "dd-MM-yyyy 00:00:00",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.joinedOn.setText(
            Html.fromHtml(
                "Joined On: <font color='#000E77'>${
                    filteredList[position].joinedon?.dateFormat(
                        "dd-MM-yyyy hh:mm:ss",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.checkout.setText(
            Html.fromHtml(
                "CheckOut: <font color='#000E77'>${
                    filteredList[position].checkout?.dateFormat(
                        "dd-MM-yyyy 00:00:00",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.rentType.setText(
            Html.fromHtml(
                "Rent Type: <font color='#000000'><b>${
                    (filteredList[position].renttype?:"").makeCamelCase()
                }</b></font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        if(filteredList[position].renttype.isNullOrEmpty()) holder.rentType.visibility = View.GONE else holder.rentType.visibility = View.VISIBLE
        val checkOut =
            filteredList[position].checkout?.dateFormat("dd-MM-yyyy 00:00:00", "dd-MM-yyyy")
        val currentDate = currentonlydate("dd-MM-yyyy")
        checkOut?.let {
            val days = calculateDaysBetween(currentDate, it)
            val rent =
                if (filteredList[position].rent.isNullOrEmpty()) 0 else filteredList[position].rent?.toInt()
            if (days < 0) {
                holder.rent.setText(
                    if (!filteredList[position].rent.isNullOrEmpty()) {
                        val rentType = filteredList[position].renttype
                        if(rentType == "monthly"){
                            var penality = calculateCharge(rent!!,1) * days
                            penality = kotlin.math.abs(penality)
                            val total = rent + penality
                            Html.fromHtml(
                                "<font color='#FF0000'>Over Due Amount AED ${rent!!}/- Penality AED ${penality}/- Total AED ${total}/-</font>"
                            )
                        }else {
                            Html.fromHtml(
                                "<font color='#FF0000'>Over Due Amount AED ${rent!! * days}/-</font>"
                            )
                        }
                    } else "AED 0/-",
                    TextView.BufferType.SPANNABLE
                )
                holder.dueDays.text = "Due ${days} Days"
            } else {
                holder.rent.setText(
                    if (!filteredList[position].rent.isNullOrEmpty()) {
                        val rentType = filteredList[position].renttype
                        if(rentType == "monthly"){
                            Html.fromHtml(
                                "<font color='#34A853'>Paid AED ${rent!!}/-</font>"
                            )
                        }else {
                            Html.fromHtml(
                                "<font color='#34A853'>Remaining Amount AED ${rent!! * days}/-</font>"
                            )
                        }
                    } else "AED 0/-",
                    TextView.BufferType.SPANNABLE
                )
                holder.dueDays.text = "Remaining ${days} Days"
            }
        }
        holder.tenantLayout.setOnLongClickListener {
            deleteClickListener?.invoke(filteredList[position])
            true
        }
        holder.contact.setOnClickListener {
            callClickListener?.invoke(filteredList[position])
        }
        holder.reminder.setOnClickListener {
            reminderClickListener?.invoke(filteredList[position])
        }
        holder.tenantLayout.setOnClickListener {
            editClickListener?.invoke(filteredList[position])
        }
        try {
            if (!filteredList[position].apartmentId.isNullOrEmpty()) {
                availableBedsClick?.invoke(filteredList[position], holder.apartment)
            } else holder.apartment.text = "Not Assigned"
        } catch (exp: Exception) {
            //
        }
        if (!filteredList[position].userImage.isNullOrEmpty()) {
            Glide.with(context)
                .load(filteredList[position].userImage)
                .transform(CircleTransformation()) // Apply custom circle transformation
                .into(holder.circularImageView)
        } else {
            Glide.with(context)
                .load(R.drawable.ic_profile)
                .transform(CircleTransformation()) // Apply custom circle transformation
                .into(holder.circularImageView)
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
        return filteredList.size
    }

    fun updateList(leadsList: List<TenantList>) {
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

    fun getList(): MutableList<TenantList> {
        return filteredList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tenantName: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.tenantName)
        var rent: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.rent)
        var apartment: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.apartment)
        var circularImageView: ImageView = itemView.findViewById<ImageView>(R.id.circularImageView)
        var joined: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.joined)
        var joinedOn: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.joinedOn)
        var checkout: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.checkout)
        var dueDays: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.dueDays)
        var rentType: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.rentType)
        var contact: AppCompatButton = itemView.findViewById<AppCompatButton>(R.id.contact)
        var reminder: AppCompatButton = itemView.findViewById<AppCompatButton>(R.id.reminder)
        var tenantLayout: ConstraintLayout =
            itemView.findViewById<ConstraintLayout>(R.id.tenantLayout)

    }
}