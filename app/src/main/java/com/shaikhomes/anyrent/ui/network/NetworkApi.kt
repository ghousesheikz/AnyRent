package com.shaikhomes.anyrent.ui.network

import com.shaikhomes.anyrent.ui.models.AddReminderData
import com.shaikhomes.anyrent.ui.models.ApartmentData
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.AvailabilityData
import com.shaikhomes.anyrent.ui.models.AvailabilityList
import com.shaikhomes.anyrent.ui.models.ExpensesData
import com.shaikhomes.anyrent.ui.models.ExpensesList
import com.shaikhomes.anyrent.ui.models.FlatData
import com.shaikhomes.anyrent.ui.models.ImageData
import com.shaikhomes.anyrent.ui.models.LeadsData
import com.shaikhomes.anyrent.ui.models.LeadsList
import com.shaikhomes.anyrent.ui.models.LeadscheduleList
import com.shaikhomes.anyrent.ui.models.PropertyData
import com.shaikhomes.anyrent.ui.models.PropertyList
import com.shaikhomes.anyrent.ui.models.ResponseData
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.models.TenantData
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.models.UserDetailsList
import com.shaikhomes.anyrent.ui.models.UserRegister
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NetworkApi {
    @GET("UserRegister?")
    fun getUserData(
        @Query("mobileno") mobileno: String,
        @Query("isactive") isactive: String
    ): Call<UserRegister>


    @GET("Property?")
    fun getProperty(
        @Query("contactno") contactno: String,
        @Query("leadid") leadid: String
    ): Call<PropertyData>

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

    @GET("Availability?")
    fun getAvailability(
        @Query("apartmentid") apartmentid: String
    ): Call<AvailabilityData>


    @GET("Apartment?")
    fun getApartments(
        @Query("userid") userid: String,
        @Query("apartmentid") apartmentid: String
    ): Call<ApartmentData>

    @POST("Property?")
    fun postProperty(@Body propertyList: PropertyList): Call<ResponseData>

    @POST("LeadSchedule?")
    fun postReminder(@Body reminderData: LeadscheduleList): Call<ResponseData>

    @POST("Leads?")
    fun postLead(@Body leadData: LeadsList): Call<ResponseData>


    @POST("Apartment?")
    fun postApartment(@Body apartmentlist: ApartmentList): Call<ResponseData>

    @POST("Availability?")
    fun postAvailability(@Body availabilityList: AvailabilityList): Call<ResponseData>

    @POST("UserRegister?")
    fun postUser(@Body userData: UserDetailsList): Call<ResponseData>

    @GET("Flat?")
    fun getFlats(
        @Query("userid") userid: String,
        @Query("apartmentid") apartmentid: String,
        @Query("floor") floor: String
    ): Call<FlatData>

    @POST("Flat?")
    fun postFlat(@Body flatList: FlatData.FlatList): Call<ResponseData>

    @GET("Rooms?")
    fun getRooms(
        @Query("apartmentid") apartmentid: String,
        @Query("floorno") floorno: String,
        @Query("flatno") flatno: String
    ): Call<RoomData>

    @POST("Rooms?")
    fun postRooms(@Body roomList: RoomData.RoomsList): Call<ResponseData>

    @GET("Expenses?")
    fun getExpenses(
        @Query("apartmentid") apartmentid: String,
        @Query("userid") userid: String,
        @Query("receivedOn") receivedOn: String
    ): Call<ExpensesData>

    @POST("Expenses?")
    fun postExpenses(@Body expensesList: ExpensesList): Call<ResponseData>

    @GET("Tenant?")
    fun getTenants(
        @Query("mobileno") mobileno: String,
        @Query("apartmentid") apartmentid: String,
        @Query("active") active: String,
        @Query("duerecords") duerecords: String
    ): Call<TenantData>

    @POST("Tenant?")
    fun postTenant(@Body roomList: TenantList): Call<ResponseData>

    @POST("Images?")
    fun postImage(@Body imageData: ImageData): Call<ResponseData>
}