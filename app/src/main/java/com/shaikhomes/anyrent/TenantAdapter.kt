package com.shaikhomes.anyrent

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
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
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.utils.calculateCharge
import com.shaikhomes.anyrent.ui.utils.calculateDaysBetween
import com.shaikhomes.anyrent.ui.utils.currentonlydate
import com.shaikhomes.anyrent.ui.utils.dateFormat
import com.shaikhomes.anyrent.ui.utils.makeCamelCase

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
                        "MM/dd/yyyy hh:mm:ss aa",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.joinedOn.setText(
            Html.fromHtml(
                "Joined On: <font color='#000000'>${
                    filteredList[position].joinedon?.dateFormat(
                        "MM/dd/yyyy hh:mm:ss aa",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.rentAmt.setText(
            Html.fromHtml(
                "Rent AED <font color='#000000'>${
                    filteredList[position].rent
                }/-</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.checkout.setText(
            Html.fromHtml(
                "CheckOut: <font color='#000E77'>${
                    filteredList[position].checkout?.dateFormat(
                        "MM/dd/yyyy hh:mm:ss aa",
                        "dd-MMM-yyyy"
                    )
                }</font>"
            ),
            TextView.BufferType.SPANNABLE
        )
        holder.rentType.setText(
            if ((filteredList[position].renttype == "monthly")) {
                Html.fromHtml(
                    "Rent Type: <font color='#FF0000'><b>${
                        (filteredList[position].renttype ?: "").makeCamelCase()
                    }</b></font>"
                )
            } else {
                Html.fromHtml(
                    "Rent Type: <font color='#34A853'><b>${
                        (filteredList[position].renttype ?: "").makeCamelCase()
                    }</b></font>"
                )
            },
            TextView.BufferType.SPANNABLE
        )
        if (filteredList[position].renttype.isNullOrEmpty()) holder.rentType.visibility =
            View.GONE else holder.rentType.visibility = View.VISIBLE
        val checkOut =
            filteredList[position].checkout?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "dd-MM-yyyy")
        val currentDate = currentonlydate("dd-MM-yyyy")
        checkOut?.let {
            val days = calculateDaysBetween(currentDate, it)
            val rent =
                if (filteredList[position].rent.isNullOrEmpty()) 0.0 else filteredList[position].rent?.toDouble()
            if (days < 0 || filteredList[position].rentstatus == "pending") {
                holder.rent.setText(
                    if (!filteredList[position].rent.isNullOrEmpty()) {
                        val rentType = filteredList[position].renttype
                        if (rentType == "monthly") {
                            var penality = calculateCharge(rent!!, 0.1) * days
                            penality = kotlin.math.abs(penality)
                            val total = rent + penality
                            val paidAmt =
                                if (filteredList[position].paid.isNullOrEmpty()) 0 else (filteredList[position].paid?:"0").toInt()
                            val dueAmt = total - paidAmt
                            if (filteredList[position].rentstatus == "pending") {
                                Html.fromHtml(
                                    "<font color='#FF0000'>Due Amount AED ${dueAmt!!}/- Paid Amount AED ${paidAmt}/-</font>"
                                )
                            } else {
                                Html.fromHtml(
                                    "<font color='#FF0000'>Over Due Amount AED ${rent!!}/- Penality AED ${penality}/-</font>"
                                )
                            }
                        } else {
                            Html.fromHtml(
                                "<font color='#FF0000'>Over Due Amount AED ${rent!! * days}/-</font>"
                            )
                        }
                    } else "AED 0/-",
                    TextView.BufferType.SPANNABLE
                )
                holder.dueDays.text = if (days > 0) "Remaining ${days} Days" else "Due ${days} Days"
            } else {
                holder.rent.setText(
                    if (!filteredList[position].rent.isNullOrEmpty()) {
                        val rentType = filteredList[position].renttype
                        if (rentType == "monthly") {
                            Html.fromHtml(
                                "<font color='#34A853'>Paid AED ${rent!!}/-</font>"
                            )
                        } else {
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
        if ((filteredList[position].renttype == "monthly")) {
            holder.checkout.visibility = View.GONE
            Log.e("onBindViewHolder: ", "true")
        }else{
            Log.e("onBindViewHolder: ", "false")
            holder.checkout.visibility = View.VISIBLE
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
        var tenantName: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.tenantName)
        var rentAmt: AppCompatTextView = itemView.findViewById<AppCompatTextView>(R.id.rentAmt)
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