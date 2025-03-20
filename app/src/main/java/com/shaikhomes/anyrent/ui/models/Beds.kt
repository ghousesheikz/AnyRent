package com.shaikhomes.anyrent.ui.models

data class Beds(
    var number: String? = "",
    var userId: String? = "",
    var occupied: Boolean? = false
) {
    override fun toString(): String {
        return number ?: ""
    }
}
