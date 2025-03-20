package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class UserDetailsList(
    @SerializedName("UserId") var UserId: Int? = null,
    @SerializedName("Active") var Active: String? = null,
    @SerializedName("IsAdmin") var IsAdmin: String? = null,
    @SerializedName("OTP") var OTP: String? = null,
    @SerializedName("UserMobileNo") var UserMobileNo: String? = null,
    @SerializedName("UserName") var UserName: String? = null,
    @SerializedName("DOB") var DOB: String? = null,
    @SerializedName("Address") var Address: String? = null,
    @SerializedName("Gender") var Gender: String? = null,
    @SerializedName("Profession") var Profession: String? = null,
    @SerializedName("GuardianType") var GuardianType: String? = null,
    @SerializedName("GName") var GName: String? = null,
    @SerializedName("GNumber") var GNumber: String? = null,
    @SerializedName("OtherInfo") var OtherInfo: String? = null,
    @SerializedName("UserImage") var UserImage: String? = null,
    @SerializedName("ProofType") var ProofType: String? = null,
    @SerializedName("ProofNumber") var ProofNumber: String? = null,
    @SerializedName("ProofImageF") var ProofImageF: String? = null,
    @SerializedName("ProofImageB") var ProofImageB: String? = null,
    @SerializedName("CreatedBy") var CreatedBy: String? = null,
    @SerializedName("UpdatedOn") var UpdatedOn: String? = null,
    @SerializedName("update") var update: String? = null,
    var leadsCount:Int=0
)
