package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class UserRegister(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("user_details_list") var userDetailsList: ArrayList<UserDetailsList> = arrayListOf()
)
