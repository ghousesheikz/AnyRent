package com.shaikhomes.anyrent.ui.models

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null
)
