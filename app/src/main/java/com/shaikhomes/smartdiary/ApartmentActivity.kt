package com.shaikhomes.smartdiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.shaikhomes.anyrent.databinding.ActivityApartmentBinding
import com.shaikhomes.smartdiary.ui.adapters.FlatAdapter
import com.shaikhomes.smartdiary.ui.adapters.FloorsAdapter
import com.shaikhomes.smartdiary.ui.adapters.RoomsAdapter
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast

class ApartmentActivity : AppCompatActivity() {
    private lateinit var activityApartmentBinding: ActivityApartmentBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList: ApartmentList? = null
    private var floorsAdapter: FloorsAdapter? = null
    private var flatAdapter: FlatAdapter? = null
    private var roomAdapter: RoomsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityApartmentBinding = ActivityApartmentBinding.inflate(layoutInflater)
        setContentView(activityApartmentBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Property Details"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        apartmentList =
            Gson().fromJson(intent.getStringExtra("apartment"), ApartmentList::class.java)
        activityApartmentBinding.apply {
            propertyName.text = apartmentList?.apartmentname
            floorsList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.HORIZONTAL, false)
            floorsAdapter = FloorsAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setFloorClickListener {
                    FloorClickListener(it)
                }
            }
            floorsList.adapter = floorsAdapter
            flatList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.HORIZONTAL, false)
            flatAdapter = FlatAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setFlatClickListener {
                    FlatClickListener(it)
                }
            }
            flatList.adapter = flatAdapter
            roomList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.HORIZONTAL, false)
            roomAdapter = RoomsAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setRoomClickListener {
                    RoomClickListener(it)
                }
            }
            roomList.adapter = roomAdapter
        }
        if (apartmentList?.nooffloors != null) {
            if (!apartmentList?.nooffloors.isNullOrEmpty()) {
                val list = apartmentList?.nooffloors?.toInt()
                val floorsList = arrayListOf<String>()
                for (i in 1..list!!) {
                    floorsList.add(i.toString())
                }
                floorsAdapter?.updateList(floorsList)
                floorsAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun RoomClickListener(room: RoomData.RoomsList) {

    }

    private fun FlatClickListener(flatList: FlatData.FlatList) {
        showToast(this, flatList.flatname.toString())
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomAdapter?.clearSelection()
                    roomAdapter?.updateList(it.roomsList)
                } else flatAdapter?.updateList(arrayListOf())
            },
            error = {
                showToast(this, it)
            },
            apartmentid = apartmentList?.ID.toString(),
            floorno = flatList.floorno!!,
            flatno = flatList.ID.toString()
        )
    }

    private fun FloorClickListener(floor: String) {
        showToast(this, floor)
        addApartmentViewModel?.getFlats(success = {
            if (it.flatList.isNotEmpty()) {
                flatAdapter?.clearSelection()
                roomAdapter?.clearSelection()
                flatAdapter?.updateList(it.flatList)
            } else flatAdapter?.updateList(arrayListOf())
        }, error = {
            showToast(this, it)
        }, prefmanager.userData?.UserId.toString(), apartmentid = apartmentList?.ID.toString())
    }
}