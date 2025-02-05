package com.shaikhomes.smartdiary.ui.models

import com.google.gson.annotations.SerializedName

class TenantData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("tenant_list") var tenant_list: ArrayList<TenantList> = arrayListOf()
)

data class TenantList(

    @SerializedName("UserId") var tenantId: Int? = null,
    @SerializedName("Active") var Active: String? = null,
    @SerializedName("MobileNo") var MobileNo: String? = null,
    @SerializedName("Name") var Name: String? = null,
    @SerializedName("apartmentId") var apartmentId: String? = null,
    @SerializedName("floorno") var floorno: String? = null,
    @SerializedName("roomno") var roomno: String? = null,
    @SerializedName("Gender") var Gender: String? = null,
    @SerializedName("Profession") var Profession: String? = null,
    @SerializedName("rent") var rent: String? = null,
    @SerializedName("rentstatus") var rentstatus: String? = null,
    @SerializedName("duedate") var duedate: String? = null,
    @SerializedName("paymentmode") var paymentmode: String? = null,
    @SerializedName("securitydeposit") var securitydeposit: String? = null,
    @SerializedName("joinedon") var joinedon: String? = null,
    @SerializedName("mailid") var mailid: String? = null,
    @SerializedName("ProofImageF") var ProofImageF: String? = null,
    @SerializedName("ProofImageB") var ProofImageB: String? = null,
    @SerializedName("CreatedBy") var CreatedBy: String? = null,
    @SerializedName("UpdatedOn") var UpdatedOn: String? = null,
    @SerializedName("checkin") var checkin: String? = null,
    @SerializedName("checkout") var checkout: String? = null,
    @SerializedName("flatno") var flatno: String? = null,
    @SerializedName("details") var details: String? = null,
    @SerializedName("iseditable") var iseditable: String? = null,
    @SerializedName("countrycode") var countrycode: String? = null,
    @SerializedName("paid") var paid: String? = null,
    @SerializedName("total") var total: String? = null,
    @SerializedName("userImage") var userImage: String? = null,
    @SerializedName("renttype") var renttype: String? = null,
    @SerializedName("update") var update: String? = null,
    @SerializedName("delete") var delete: String? = null

)