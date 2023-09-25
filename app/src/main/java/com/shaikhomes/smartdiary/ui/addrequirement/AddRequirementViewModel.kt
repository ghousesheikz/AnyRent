package com.shaikhomes.smartdiary.ui.addrequirement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddRequirementViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text


    fun addProperty(
        propertyList: PropertyList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postProperty(propertyList)
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
}