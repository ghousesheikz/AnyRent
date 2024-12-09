package com.shaikhomes.smartdiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityApartmentBinding
import com.shaikhomes.anyrent.databinding.ActivityRoomsAvailableBinding
import com.shaikhomes.smartdiary.ui.adapters.RoomsAdapter
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast

class RoomsAvailableActivity : AppCompatActivity() {
    private lateinit var activityRoomsBinding: ActivityRoomsAvailableBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var roomAdapter: RoomsAdapter? = null
    val type = object : TypeToken<ArrayList<String>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRoomsBinding = ActivityRoomsAvailableBinding.inflate(layoutInflater)
        setContentView(activityRoomsBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Room Details"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        activityRoomsBinding.apply {
            roomAvailableList.layoutManager =
                LinearLayoutManager(this@RoomsAvailableActivity)
            roomAdapter = RoomsAdapter(this@RoomsAvailableActivity, arrayListOf(), true,true).apply {
                setBedClickListener { roomsList, beds ->
                    val intent = Intent(this@RoomsAvailableActivity, TenantRegistration::class.java)
                    intent.putExtra("ROOM_SELECT",Gson().toJson(roomsList))
                    intent.putExtra("BED_SELECT",Gson().toJson(beds))
                    startActivity(intent)
                    finish()
                }
            }
            roomAvailableList.adapter = roomAdapter
        }
        getRooms()
    }

    private fun getRooms() {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomAdapter?.updateList(it.roomsList)
                } else roomAdapter?.updateList(arrayListOf())
            },
            error = {
                showToast(this, it)
            },
            apartmentid = prefmanager.selectedApartment?.ID.toString(),
            floorno = "",
            flatno = ""
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }
}