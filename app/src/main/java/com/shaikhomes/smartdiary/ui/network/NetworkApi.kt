package com.shaikhomes.smartdiary.ui.network

import com.shaikhomes.smartdiary.ui.models.AddReminderData
import com.shaikhomes.smartdiary.ui.models.LeadsData
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.LeadscheduleList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.models.UserRegister
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NetworkApi {
    @GET("UserRegister?")
    fun getUserData(@Query("mobileno") mobileno: String): Call<UserRegister>


    @GET("Property?")
    fun getProperty(@Query("contactno") contactno: String,@Query("leadid") leadid: String): Call<PropertyData>

    @GET("Leads?")
    fun getleadsData(
        @Query("assignto") assignto: String,
        @Query("priority") priority: String,
        @Query("typeoflead") typeoflead: String,
        @Query("contactno") contactno: String,
        @Query("status") status: String
    ): Call<LeadsData>

    @GET("LeadSchedule?")
    fun getScheduleLeads(
        @Query("assignto") assignto: String,
        @Query("scheduledon") scheduledon: String,
        @Query("contactno") contactno: String
    ): Call<AddReminderData>

    @POST("Property?")
    fun postProperty(@Body propertyList: PropertyList): Call<ResponseData>

    @POST("LeadSchedule?")
    fun postReminder(@Body reminderData: LeadscheduleList): Call<ResponseData>

    @POST("Leads?")
    fun postLead(@Body leadData: LeadsList): Call<ResponseData>

    @POST("UserRegister?")
    fun postUser(@Body userData: UserDetailsList): Call<ResponseData>
}