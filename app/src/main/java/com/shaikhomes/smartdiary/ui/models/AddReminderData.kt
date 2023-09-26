package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class AddReminderData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("leadschedule_list") var leadscheduleList: ArrayList<LeadscheduleList> = arrayListOf()
)

data class LeadscheduleList(

    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("contactnumber") var contactnumber: String? = null,
    @SerializedName("activity") var activity: String? = null,
    @SerializedName("leadsname") var leadsname: String? = null,
    @SerializedName("createdon") var createdon: String? = null,
    @SerializedName("scheduledon") var scheduledon: String? = null,
    @SerializedName("feedback") var feedback: String? = null,
    @SerializedName("createdby") var createdby: String? = null,
    @SerializedName("assignto") var assignto: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("update") var update: String? = null,
    @SerializedName("delete") var delete: String? = null

)
