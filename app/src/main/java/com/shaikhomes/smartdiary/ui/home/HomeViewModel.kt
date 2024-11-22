package com.shaikhomes.smartdiary.ui.home

import androidx.lifecycle.ViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    fun getApartments(
        success: (ApartmentData) -> Unit,
        error: (String) -> Unit,
        userid: String, apartmentid: String
    ) {
        RetrofitInstance.api.getApartments(userid, apartmentid)
            .enqueue(object : Callback<ApartmentData> {
                override fun onResponse(
                    call: Call<ApartmentData>,
                    response: Response<ApartmentData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<ApartmentData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

    fun getRooms(
        success: (RoomData) -> Unit,
        error: (String) -> Unit,
        apartmentid: String, floorno: String, flatno: String
    ) {
        RetrofitInstance.api.getRooms(apartmentid, floorno, flatno)
            .enqueue(object : Callback<RoomData> {
                override fun onResponse(
                    call: Call<RoomData>,
                    response: Response<RoomData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<RoomData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }
}