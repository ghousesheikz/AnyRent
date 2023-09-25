package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class UserDetailsList(
    @SerializedName("UserId") var UserId: Int? = null,
    @SerializedName("Active") var Active: String? = null,
    @SerializedName("IsAdmin") var IsAdmin: String? = null,
    @SerializedName("OTP") var OTP: String? = null,
    @SerializedName("UserMobileNo") var UserMobileNo: String? = null,
    @SerializedName("UserName") var UserName: String? = null,
    @SerializedName("Address") var Address: String? = null,
    @SerializedName("rent") var rent: String? = null,
    @SerializedName("agreement") var agreement: String? = null,
    @SerializedName("paid_status") var paidStatus: String? = null,
    @SerializedName("agreement_status") var agreementStatus: String? = null,
    @SerializedName("txm_image") var txmImage: String? = null,
    @SerializedName("month_rent") var monthRent: String? = null,
    @SerializedName("update") var update: String? = null,
    var leadsCount: Int? = 0
)
