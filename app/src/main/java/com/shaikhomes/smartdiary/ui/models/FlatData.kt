package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class FlatData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("flat_list") var flatList: ArrayList<FlatList> = arrayListOf()
) {
    data class FlatList(

        @SerializedName("ID") var ID: Int? = null,
        @SerializedName("userid") var userid: String? = null,
        @SerializedName("apartmentid") var apartmentid: String? = null,
        @SerializedName("flatname") var flatname: String? = null,
        @SerializedName("floorno") var floorno: String? = null,
        @SerializedName("createdby") var createdby: String? = null,
        @SerializedName("updatedon") var updatedon: String? = null,
        @SerializedName("update") var update: String? = null

    ) {
        override fun toString(): String {
            return flatname?:""
        }
    }


}
