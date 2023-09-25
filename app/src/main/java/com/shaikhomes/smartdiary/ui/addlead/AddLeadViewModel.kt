package com.shaikhomes.smartdiary.ui.addlead

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.shaikhomes.smartdiary.MainActivity
import com.shaikhomes.smartdiary.ui.customviews.dismissProgress
import com.shaikhomes.smartdiary.ui.customviews.showProgress
import com.shaikhomes.smartdiary.ui.models.LeadsData
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.UserRegister
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddLeadViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    fun addLead(
        leadsList: LeadsList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postLead(leadsList)
            .enqueue(object : Callback<ResponseData> {
                override fun onResponse(
                    call: Call<ResponseData>,
                    response: Response<ResponseData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else if (response.body()?.message == "Lead already exists") {
                        error.invoke("Lead already exists")
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

    fun getLeads(
        contactno: String,
        success: (LeadsData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.getleadsData("", "", "", contactno)
            .enqueue(object : Callback<LeadsData> {
                override fun onResponse(
                    call: Call<LeadsData>,
                    response: Response<LeadsData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<LeadsData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }
}