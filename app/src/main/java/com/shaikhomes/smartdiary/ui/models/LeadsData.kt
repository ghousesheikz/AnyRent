package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class LeadsData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("leads_list") var leadsList: ArrayList<LeadsList> = arrayListOf()
)
