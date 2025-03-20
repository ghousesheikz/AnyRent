package com.shaikhomes.anyrent.ui.models

data class Capacity(
    var name: String? = "",
    var number: String? = ""
){
    override fun toString(): String {
        return name?:""
    }
}
