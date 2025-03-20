package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class AvailabilityData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("availability_list") var availabilityList: ArrayList<AvailabilityList> = arrayListOf()
)

data class AvailabilityList(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("apartmentid") var apartmentid: String? = null,
    @SerializedName("apartmentname") var apartmentname: String? = null,
    @SerializedName("apartmentfor") var apartmentfor: String? = null,
    @SerializedName("flat") var flat: String? = null,
    @SerializedName("room1") var room1: String? = null,
    @SerializedName("room2") var room2: String? = null,
    @SerializedName("room3") var room3: String? = null,
    @SerializedName("room4") var room4: String? = null,
    @SerializedName("createdby") var createdby: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("update") var update: String? = null

)
