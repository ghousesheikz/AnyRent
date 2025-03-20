package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class ApartmentData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("apartment_list") var apartmentList: ArrayList<ApartmentList> = arrayListOf()
)

data class ApartmentList(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("userid") var userid: String? = null,
    @SerializedName("apartmentname") var apartmentname: String? = null,
    @SerializedName("apartmentfor") var apartmentfor: String? = null,
    @SerializedName("nooffloors") var nooffloors: String? = null,
    @SerializedName("createdby") var createdby: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("update") var update: String? = null

) {
    override fun toString(): String {
        return apartmentname?:""
    }
}