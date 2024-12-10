package com.shaikhomes.smartdiary.ui.apartment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentData
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.ExpensesData
import com.shaikhomes.smartdiary.ui.models.ExpensesList
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.models.TenantData
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddApartmentViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

    fun addApartment(
        leadsList: ApartmentList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postApartment(leadsList)
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

    fun addTenant(
        leadsList: TenantList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postTenant(leadsList)
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

    fun addFlat(
        leadsList: FlatData.FlatList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postFlat(leadsList)
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

    fun addRooms(
        leadsList: RoomData.RoomsList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postRooms(leadsList)
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

    fun getTenants(
        success: (TenantData) -> Unit,
        error: (String) -> Unit,
        mobileNo: String, apartmentid: String, active: String, duerecords: String
    ) {
        RetrofitInstance.api.getTenants(mobileNo, apartmentid, active, duerecords)
            .enqueue(object : Callback<TenantData> {
                override fun onResponse(
                    call: Call<TenantData>,
                    response: Response<TenantData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<TenantData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

    fun getFlats(
        success: (FlatData) -> Unit,
        error: (String) -> Unit,
        userid: String, apartmentid: String, floorno: String
    ) {
        RetrofitInstance.api.getFlats(userid, apartmentid, floorno)
            .enqueue(object : Callback<FlatData> {
                override fun onResponse(
                    call: Call<FlatData>,
                    response: Response<FlatData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<FlatData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }

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

    fun addExpenses(
        expenseList: ExpensesList, success: (ResponseData) -> Unit,
        error: (String) -> Unit
    ) {
        RetrofitInstance.api.postExpenses(expenseList)
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

    fun getExpenses(
        success: (ExpensesData) -> Unit,
        error: (String) -> Unit,
        apartmentid: String, userid: String, receivedOn: String
    ) {
        RetrofitInstance.api.getExpenses(apartmentid, userid, receivedOn)
            .enqueue(object : Callback<ExpensesData> {
                override fun onResponse(
                    call: Call<ExpensesData>,
                    response: Response<ExpensesData>
                ) {
                    if (response.body()?.status == "200") {
                        success.invoke(response.body()!!)
                    } else {
                        error.invoke("Something Went Wrong")
                    }
                }

                override fun onFailure(call: Call<ExpensesData>, t: Throwable) {
                    error.invoke(t.message.toString())
                }
            })
    }
}