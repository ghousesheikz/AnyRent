package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

data class LeadsList(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("contactnumber") var contactnumber: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("leadsname") var leadsname: String? = null,
    @SerializedName("locations") var locations: String? = null,
    @SerializedName("maxamount") var maxamount: String? = null,
    @SerializedName("minamount") var minamount: String? = null,
    @SerializedName("priority") var priority: String? = null,
    @SerializedName("project") var project: String? = null,
    @SerializedName("registerno") var registerno: String? = null,
    @SerializedName("typeoflead") var typeoflead: String? = null,
    @SerializedName("calldetails") var calldetails: String? = null,
    @SerializedName("leadstatus") var leadstatus: String? = null,
    @SerializedName("leadrole") var leadrole: String? = null,
    @SerializedName("createdby") var createdby: String? = null,
    @SerializedName("assignto") var assignto: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("countrycode") var countrycode: String? = null,
    @SerializedName("lookingfor") var lookingfor: String? = null,
    @SerializedName("update") var update: String? = null,
    @SerializedName("delete") var delete: String? = null,
    var propertyData: String? = ""

)
