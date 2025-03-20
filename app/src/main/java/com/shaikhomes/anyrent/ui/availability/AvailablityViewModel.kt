package com.shaikhomes.anyrent.ui.availability

import androidx.lifecycle.ViewModel
import com.shaikhomes.anyrent.ui.models.AvailabilityData
import com.shaikhomes.anyrent.ui.models.AvailabilityList
import com.shaikhomes.anyrent.ui.models.ResponseData
import com.shaikhomes.anyrent.ui.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AvailablityViewModel : ViewModel() {

    fun getAvailabilities(
        apartmentid: String,
        success: (AvailabilityData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.getAvailability(apartmentid)
            .enqueue(object : Callback<AvailabilityData> {
                override fun onResponse(
                    call: Call<AvailabilityData>,
                    response: Response<AvailabilityData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<AvailabilityData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }


    fun postAvailability(
        availabilityList: AvailabilityList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postAvailability(availabilityList)
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