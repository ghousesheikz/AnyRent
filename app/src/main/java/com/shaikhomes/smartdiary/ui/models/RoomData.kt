package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class RoomData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("rooms_list") var roomsList: ArrayList<RoomsList> = arrayListOf()
) {
    data class RoomsList(
        @SerializedName("ID") var ID: Int? = null,
        @SerializedName("apartmentid") var apartmentid: String? = null,
        @SerializedName("roomname") var roomname: String? = null,
        @SerializedName("roomcapacity") var roomcapacity: String? = null,
        @SerializedName("roomtype") var roomtype: String? = null,
        @SerializedName("rentperday") var rentperday: String? = null,
        @SerializedName("rentpermonth") var rentpermonth: String? = null,
        @SerializedName("occupied") var occupied: String? = null,
        @SerializedName("available") var available: String? = null,
        @SerializedName("createdby") var createdby: String? = null,
        @SerializedName("updatedon") var updatedon: String? = null,
        @SerializedName("floorno") var floorno: String? = null,
        @SerializedName("flatno") var flatno: String? = null,
        @SerializedName("update") var update: String? = null
    ){
        override fun toString(): String {
            return roomname?:""
        }
    }
}
