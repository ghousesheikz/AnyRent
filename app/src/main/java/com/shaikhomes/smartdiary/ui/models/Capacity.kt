package com.shaikhomes.smartdiary.ui.models

data class Capacity(
    var name: String? = "",
    var number: String? = ""
){
    override fun toString(): String {
        return name?:""
    }
}
