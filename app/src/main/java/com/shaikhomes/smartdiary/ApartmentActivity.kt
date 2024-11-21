package com.shaikhomes.smartdiary

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityApartmentBinding
import com.shaikhomes.smartdiary.ui.adapters.FlatAdapter
import com.shaikhomes.smartdiary.ui.adapters.FloorsAdapter
import com.shaikhomes.smartdiary.ui.adapters.RoomsAdapter
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.hideKeyboard
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
            addFlat.setOnClickListener {
                addFloorFlat()
                menuGreen.close(true)
            }
            addFloor.setOnClickListener {
                addPropertyFloor()
                menuGreen.close(true)
            }
            addRoom.setOnClickListener {
                addFlatRooms()
                menuGreen.close(true)
            }
        }
        getFloors()
    }

    private fun addFlatRooms() {
        val floorList: ArrayList<String>? = arrayListOf()
        val roomTypeList: ArrayList<String>? = arrayListOf()
        val FlayList: ArrayList<FlatData.FlatList>? = arrayListOf()
        FlayList?.add(FlatData.FlatList(ID=-1,flatname = "Select Flat"))
        roomTypeList?.add("Select Room Type")
        roomTypeList?.add("Regular")
        roomTypeList?.add("Deluxe")
        roomTypeList?.add("Ac")
        roomTypeList?.add("Non-AC")
        floorList?.addAll(getFloorData())
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_rooms, null)
        val floorSpinner = view.findViewById<Spinner>(R.id.floorSpinner)
        val roomtypeSpinner = view.findViewById<Spinner>(R.id.roomtypeSpinner)
        floorSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, floorList!!
        )
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFor = if (selectedItem != "Select Floor") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        roomtypeSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, roomTypeList!!
        )

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
    }

    private fun addFloorFlat() {
        val listFor: ArrayList<String>? = arrayListOf()
        listFor?.addAll(getFloorData())
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_property, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val txtAddProperty = view.findViewById<TextView>(R.id.txtAddProperty)
        val spinnerFor = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextNoOfFloors = view.findViewById<EditText>(R.id.editTextNoOfFloors)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        txtAddProperty.text = "Add Flat"
        submitButton.text = "Add Flat"
        editTextName.hint = "Flat Name"
        editTextNoOfFloors.visibility = View.GONE
        spinnerFor.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, listFor!!
        )
        spinnerFor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFor = if (selectedItem != "Select Floor") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        submitButton.setOnClickListener {
            val name = editTextName.text.toString()
            val noOfFloors = editTextNoOfFloors.text.toString()
            if (name.isBlank() || selectFor.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(it)
                val apartmentData = FlatData.FlatList(
                    ID = apartmentList?.ID,
                    userid = prefmanager.userData?.UserId.toString(),
                    apartmentid = apartmentList?.ID.toString(),
                    flatname = name,
                    floorno = selectFor,
                    createdby = prefmanager.userData?.UserName,
                    updatedon = currentdate()
                )
                addApartmentViewModel?.addFlat(apartmentData, success = {
                    if (it.status == "200") {
                        editTextName.setText("")
                        editTextNoOfFloors.setText("")
                        getApartments()
                    }
                    bottomSheetDialog.dismiss()
                }, error = {
                    showToast(this, it)
                    bottomSheetDialog.dismiss()
                })
            }
        }
        bottomSheetDialog.behavior.peekHeight = 900
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
    }

    private fun getFloors() {
        if (apartmentList?.nooffloors != null) {
            if (!apartmentList?.nooffloors.isNullOrEmpty()) {
                val list = apartmentList?.nooffloors?.toInt()
                val floorsList = arrayListOf<String>()
                for (i in 1..list!!) {
                    floorsList.add(i.toString())
                }
                floorsAdapter?.updateList(floorsList)
                floorsAdapter?.notifyDataSetChanged()
                floorsAdapter?.clearSelection()
                flatAdapter?.clearSelection()
                roomAdapter?.clearSelection()
            }
        }
    }

    private fun addPropertyFloor() {
        val listFor: ArrayList<String>? = arrayListOf()
        apartmentList?.apartmentfor?.let { listFor?.add(it) }
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_property, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val txtAddProperty = view.findViewById<TextView>(R.id.txtAddProperty)
        val spinnerFor = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextNoOfFloors = view.findViewById<EditText>(R.id.editTextNoOfFloors)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        editTextNoOfFloors.setText(apartmentList?.nooffloors)
        editTextName.setText(apartmentList?.apartmentname)
        txtAddProperty.text = "Update Property"
        submitButton.text = "Update"
        editTextName.isEnabled = false
        spinnerFor.isEnabled = false
        spinnerFor.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, listFor!!
        )
        spinnerFor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFor = if (selectedItem != "Select") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        submitButton.setOnClickListener {
            val name = editTextName.text.toString()
            val noOfFloors = editTextNoOfFloors.text.toString()
            if (name.isBlank() || selectFor.isBlank() || noOfFloors.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(it)
                val apartmentData = ApartmentList(
                    ID = apartmentList?.ID,
                    userid = prefmanager.userData?.UserId.toString(),
                    apartmentname = name.trim(),
                    apartmentfor = selectFor,
                    nooffloors = noOfFloors,
                    createdby = prefmanager.userData?.UserName,
                    updatedon = currentdate(),
                    update = "update"
                )
                addApartmentViewModel?.addApartment(apartmentData, success = {
                    if (it.status == "200") {
                        editTextName.setText("")
                        editTextNoOfFloors.setText("")
                        getApartments()
                    }
                    bottomSheetDialog.dismiss()
                }, error = {
                    showToast(this, it)
                    bottomSheetDialog.dismiss()
                })
            }
        }
        bottomSheetDialog.behavior.peekHeight = 900
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
    }

    private fun getApartments() {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList = it.apartmentList.first()
                    getFloors()
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentList?.ID.toString()
        )
    }

    private fun RoomClickListener(room: RoomData.RoomsList) {

    }

    fun getFloorData(): ArrayList<String> {
        val list = arrayListOf<String>()
        list.add("Select Floor")
        for (item in 1..apartmentList?.nooffloors!!.toInt()) {
            list.add(item.toString())
        }
        return list
    }

    private fun FlatClickListener(flatList: FlatData.FlatList) {
        showToast(this, flatList.flatname.toString())
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomAdapter?.clearSelection()
                    roomAdapter?.updateList(it.roomsList)
                } else roomAdapter?.updateList(arrayListOf())
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
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatAdapter?.clearSelection()
                    roomAdapter?.clearSelection()
                    flatAdapter?.updateList(it.flatList)
                } else flatAdapter?.updateList(arrayListOf())
            },
            error = {
                showToast(this, it)
            },
            prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentList?.ID.toString(),
            floor
        )
    }
}