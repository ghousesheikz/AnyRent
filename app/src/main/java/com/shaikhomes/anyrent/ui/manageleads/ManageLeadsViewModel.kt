package com.shaikhomes.anyrent.ui.manageleads

import androidx.lifecycle.ViewModel
import com.shaikhomes.anyrent.ui.models.LeadsData
import com.shaikhomes.anyrent.ui.models.LeadsList
import com.shaikhomes.anyrent.ui.models.PropertyData
import com.shaikhomes.anyrent.ui.models.ResponseData
import com.shaikhomes.anyrent.ui.models.UserRegister
import com.shaikhomes.anyrent.ui.network.RetrofitInstance
import com.shaikhomes.anyrent.ui.utils.currentdate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageLeadsViewModel : ViewModel() {
    fun getLeads(
        priority: String,
        typeoflead: String,
        status:String,
        success: (LeadsData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.getleadsData("", priority, typeoflead, "",status)
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

    fun getUsers(success: (UserRegister) -> Unit, error: (String) -> Unit) {
        RetrofitInstance.api.getUserData("","")
            .enqueue(object : Callback<UserRegister> {
                override fun onResponse(
                    call: Call<UserRegister>,
                    response: Response<UserRegister>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<UserRegister>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

    fun updateLead(
        leadsList: LeadsList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        leadsList.date = currentdate()
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
}