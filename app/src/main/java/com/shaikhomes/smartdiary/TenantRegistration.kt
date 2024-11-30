package com.shaikhomes.smartdiary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.databinding.ActivityTenantRegistrationBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.Beds
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast

class TenantRegistration : AppCompatActivity() {
    private lateinit var activityTenantsBinding: ActivityTenantRegistrationBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    val type = object : TypeToken<ArrayList<String>>() {}.type
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var roomsList: RoomData.RoomsList? = null
    private var selectedBed: Beds? = null
    private var apartmentList: ApartmentList? = null
    private var flatList: FlatData.FlatList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityTenantsBinding = ActivityTenantRegistrationBinding.inflate(layoutInflater)
        setContentView(activityTenantsBinding.root)
        supportActionBar?.title = "Tenants"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        roomsList = Gson().fromJson(intent.getStringExtra("ROOM_SELECT"), RoomData.RoomsList::class.java)
        selectedBed = Gson().fromJson(intent.getStringExtra("BED_SELECT"), Beds::class.java)
        activityTenantsBinding.apply {

        }
        getApartment(roomsList?.apartmentid)
        getFlat(roomsList?.apartmentid,roomsList?.floorno)
    }

    private fun getApartment(apartmentid: String?) {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList = it.apartmentList.first()
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid =apartmentid!!
        )
    }

    private fun getFlat(apartmentid: String?,floorNo: String?) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatList = it.flatList.first()
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentid!!, floorno = floorNo!!
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

}