package com.shaikhomes.smartdiary.ui.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.models.ApartmentList

class ApartmentAdapter(
    private val context: Context,
    private val leadsList: ArrayList<ApartmentList>,
    private val isAdmin: Boolean? = false
) :
    RecyclerView.Adapter<ApartmentAdapter.LeadViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApartmentAdapter.LeadViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.apartment_layout, parent, false)
        return LeadViewHolder(view)
    }

    private var editClickListener: ((ApartmentList) -> Unit)? = null
    private var availableBedsClick: ((ApartmentList, AppCompatTextView) -> Unit)? = null

    fun setEditClickListener(leadList: (ApartmentList) -> Unit) {
        this.editClickListener = leadList
    }

    fun setAvailableClickListener(leadList: (ApartmentList, AppCompatTextView) -> Unit) {
        this.availableBedsClick = leadList
    }

    private var propertyClickListener: ((ApartmentList) -> Unit)? = null

    fun setPropertyClickListener(leadList: (ApartmentList) -> Unit) {
        this.propertyClickListener = leadList
    }

    private var infoClickListener: ((ApartmentList) -> Unit)? = null

    fun setDeleteClickListener(leadList: (ApartmentList) -> Unit) {
        this.infoClickListener = leadList
    }


    override fun onBindViewHolder(holder: LeadViewHolder, position: Int) {
        holder.apartmentName.setText(
            Html.fromHtml("Name: <font color='#000E77'>${leadsList[position].apartmentname}</font>"),
            TextView.BufferType.SPANNABLE
        )
        holder.apartmentFor.setText(
            Html.fromHtml("For: <font color='#000E77'>${leadsList[position].apartmentfor}</font>"),
            TextView.BufferType.SPANNABLE
        )
//        holder.noOfFloors.setText(
//            Html.fromHtml("Available: <font color='#000E77'>${getFloorData(leadsList[position].nooffloors ?: "")}</font>"),
//            TextView.BufferType.SPANNABLE
//        )

        holder.ImgEdit.setOnClickListener {
            editClickListener?.invoke(leadsList[position])
        }
        holder.itemView.rootView.setOnClickListener {
            propertyClickListener?.invoke(leadsList[position])
        }

        holder.ImgDelete.setOnClickListener {
            infoClickListener?.invoke(leadsList[position])
        }
        try {
            availableBedsClick?.invoke(leadsList[position], holder.noOfFloors)
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

    fun updateList(leadsList: List<ApartmentList>) {
        this.leadsList.apply {
            if (this.size > 0) this.clear()
            this.addAll(leadsList)
            notifyDataSetChanged()
        }
    }

    fun getList(): ArrayList<ApartmentList> {
        return leadsList
    }

    inner class LeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var apartmentName: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.apartmentName)
        var apartmentFor: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.apartmentFor)
        var noOfFloors: AppCompatTextView =
            itemView.findViewById<AppCompatTextView>(R.id.noOfFloors)
        var ImgEdit: ImageButton = itemView.findViewById<ImageButton>(R.id.Imgedit)
        var ImgQrCode: ImageButton = itemView.findViewById<ImageButton>(R.id.ImgQrCode)
        var ImgDelete: ImageButton = itemView.findViewById<ImageButton>(R.id.ImgDelete)
    }
}