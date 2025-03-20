package com.shaikhomes.anyrent.ui.leadinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shaikhomes.anyrent.ui.models.AddReminderData
import com.shaikhomes.anyrent.ui.models.LeadsData
import com.shaikhomes.anyrent.ui.models.LeadsList
import com.shaikhomes.anyrent.ui.models.PropertyData
import com.shaikhomes.anyrent.ui.models.ResponseData
import com.shaikhomes.anyrent.ui.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeadInfoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    fun deleteLead(
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
        RetrofitInstance.api.getleadsData("", "", "", contactno,"")
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

    fun getProperties(contactno: String, success: (PropertyData) -> Unit, error: (String) -> Unit) {
        RetrofitInstance.api.getProperty(contactno,"")
            .enqueue(object : Callback<PropertyData> {
                override fun onResponse(
                    call: Call<PropertyData>,
                    response: Response<PropertyData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<PropertyData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

    fun getLeadSchedule(contactno: String, success: (AddReminderData) -> Unit, error: (String) -> Unit) {
        RetrofitInstance.api.getScheduleLeads("","",contactno)
            .enqueue(object : Callback<AddReminderData> {
                override fun onResponse(
                    call: Call<AddReminderData>,
                    response: Response<AddReminderData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<AddReminderData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }
}