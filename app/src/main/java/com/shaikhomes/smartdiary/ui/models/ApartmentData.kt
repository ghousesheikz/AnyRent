package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class ApartmentData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("apartment_list") var apartmentList: ArrayList<ApartmentList> = arrayListOf()
)

data class ApartmentList(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("apartmentid") var apartmentid: String? = null,
    @SerializedName("apartmentname") var apartmentname: String? = null,
    @SerializedName("apartmentfor") var apartmentfor: String? = null,
    @SerializedName("createdby") var createdby: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("update") var update: String? = null

)