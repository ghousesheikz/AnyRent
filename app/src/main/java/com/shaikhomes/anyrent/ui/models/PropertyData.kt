package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class PropertyData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("property_list") var propertyList: ArrayList<PropertyList> = arrayListOf()
)