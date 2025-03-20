package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class ExpensesData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("expenses_list") var expensesList: ArrayList<ExpensesList> = arrayListOf()
)

data class ExpensesList(
    @SerializedName("ID") var ID: Int? = null,
    @SerializedName("userid") var userid: String? = null,
    @SerializedName("apartmentid") var apartmentid: String? = null,
    @SerializedName("creditAmount") var creditAmount: String? = null,
    @SerializedName("category") var category: String? = null,
    @SerializedName("debitAmount") var debitAmount: String? = null,
    @SerializedName("receivedOn") var receivedOn: String? = null,
    @SerializedName("paymentMode") var paymentMode: String? = null,
    @SerializedName("txnId") var txnId: String? = null,
    @SerializedName("notes") var notes: String? = null,
    @SerializedName("picture") var picture: String? = null,
    @SerializedName("receivedBy") var receivedBy: String? = null,
    @SerializedName("updatedon") var updatedon: String? = null,
    @SerializedName("update") var update: String? = null
)
