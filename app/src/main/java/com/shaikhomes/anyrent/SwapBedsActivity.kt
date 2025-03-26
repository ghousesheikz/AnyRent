package com.shaikhomes.anyrent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.databinding.ActivitySwapBedsBinding
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.customviews.SafeClickListener
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.FlatData
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.dateFormat
import com.shaikhomes.anyrent.ui.utils.showToast

class SwapBedsActivity : AppCompatActivity() {
    private lateinit var activitySwapBedsBinding: ActivitySwapBedsBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var tenantList: TenantList? = null
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList = arrayListOf<ApartmentList>()
    private var floorList = arrayListOf<String>()
    private var flatList = arrayListOf<FlatData.FlatList>()
    private var roomList = arrayListOf<RoomData.RoomsList>()
    private var bedsList = arrayListOf<Beds>()
    private var selectApart: String? = null
    private var selectFloor: String? = null
    private var selectFlat: String? = null
    private var selectRoom: String? = null
    private var selectBed: String? = null
    private var apartmentSelected: ApartmentList? = null
    private var bedSelected: Beds? = null
    private var FlatSelected: FlatData.FlatList? = null
    private var roomSelected: RoomData.RoomsList? = null
    val type = object : TypeToken<ArrayList<String>>() {}.type
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySwapBedsBinding = ActivitySwapBedsBinding.inflate(layoutInflater)
        setContentView(activitySwapBedsBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Swap Beds"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        tenantList =
            Gson().fromJson(intent.getStringExtra("swap_tenant"), TenantList::class.java)
        activitySwapBedsBinding.editName.apply {
            setText(tenantList?.Name)
            isEnabled = false
        }
        activitySwapBedsBinding.textDropDownChooseCountry.apply {
            setText(tenantList?.countrycode)
            isEnabled = false
        }
        activitySwapBedsBinding.editNumber.apply {
            setText(tenantList?.MobileNo)
            isEnabled = false
        }
        apartmentList.add(ApartmentList(ID = -1, apartmentname = "Select Apartment"))
        floorList.add("Select Floor")
        flatList.add(FlatData.FlatList(ID = -1, flatname = "Select Flat"))
        roomList.add(RoomData.RoomsList(ID = -1, roomname = "Select Room"))
        bedsList.add(Beds(number = "Available Beds"))
        activitySwapBedsBinding.apartmentSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, apartmentList!!
        )
        activitySwapBedsBinding.apartmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectApart = if (selectedItem != "Select Apartment") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activitySwapBedsBinding.floorSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, floorList!!
        )
        activitySwapBedsBinding.floorSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectFloor = if (selectedItem != "Select Floor") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activitySwapBedsBinding.flatSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, flatList!!
        )
        activitySwapBedsBinding.flatSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectFlat = if (selectedItem != "Select Flat") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activitySwapBedsBinding.roomSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, roomList!!
        )
        activitySwapBedsBinding.roomSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectRoom = if (selectedItem != "Select Room") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activitySwapBedsBinding.bedsSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, bedsList!!
        )
        activitySwapBedsBinding.bedsSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectBed = if (selectedItem != "Available Beds") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activitySwapBedsBinding.btnUpdate.setOnClickListener(safeClickListener)
        getApartments()
    }

    private fun getApartments() {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList.clear()
                    it.apartmentList.add(
                        0,
                        ApartmentList(ID = -1, apartmentname = "Select Apartment")
                    )
                    apartmentList.addAll(it.apartmentList)
                    if (apartmentList.isNotEmpty()) {
                        apartmentList.filter { it.ID.toString() == tenantList?.apartmentId }
                            .let { apartmentList ->
                                if (apartmentList.isNotEmpty()) {
                                    if (tenantList?.details.isNullOrEmpty()) {
                                        activitySwapBedsBinding.apartment.text =
                                            apartmentList.first().apartmentname
                                    } else activitySwapBedsBinding.apartment.text =
                                        apartmentList.first().apartmentname.plus(" - ${tenantList?.details} (Current Bed)")
                                }
                            }
                    }
                    activitySwapBedsBinding.apartmentSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, apartmentList!!
                    )
                    activitySwapBedsBinding.apartmentSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectApart = if (selectedItem != "Select Apartment") {
                                    apartmentSelected = apartmentList[p2]
                                    getFloors(apartmentSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = ""
        )
    }

    private fun getFloors(apartmentSelected: ApartmentList) {
        if (apartmentSelected?.nooffloors != null) {
            if (!apartmentSelected?.nooffloors.isNullOrEmpty()) {
                val list: ArrayList<String> = Gson().fromJson(apartmentSelected?.nooffloors, type)
                list.add(0, "Select Floor")
                floorList.clear()
                floorList.addAll(list)
                activitySwapBedsBinding.floorSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, floorList!!
                )
                activitySwapBedsBinding.floorSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            val selectedItem = p0?.getItemAtPosition(p2).toString()
                            selectFloor = if (selectedItem != "Select Floor") {
                                getFlats(selectedItem!!)
                                selectedItem
                            } else ""
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }
        }
    }

    val safeClickListener = SafeClickListener {
        tenantList?.let { it1 ->
            deleteTenant(it1, {
                it1.apartmentId = apartmentSelected?.ID.toString()
                it1.floorno = selectFloor
                it1.roomno = roomSelected?.ID.toString()
                it1.flatno = FlatSelected?.ID.toString()
                it1.details = "${roomSelected?.roomname} - B${bedSelected?.number}"
                it1.CreatedBy = prefmanager.userData?.UserName
                it1.UpdatedOn = currentdate()
                it1?.checkin =
                    it1?.checkin?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                it1?.checkout =
                    it1?.checkout?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                it1?.UpdatedOn = currentdate()
                it1?.CreatedBy = prefmanager?.userData?.UserName
                it1?.duedate =
                    it1?.duedate?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                it1?.joinedon =
                    it1?.joinedon?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                it1?.update = "update"
                addApartmentViewModel?.addTenant(it1, success = {
                    updatebeds(
                        roomSelected,
                        bedSelected,
                        activitySwapBedsBinding.editNumber.text.toString()
                    )
                }, error = {
                    showToast(this, it)
                })
            })

        }
    }

    private fun updatebeds(
        roomSelected: RoomData.RoomsList?,
        bedSelected: Beds?,
        mobNumber: String
    ) {
        if (roomSelected?.available != null) {
            if (!roomSelected.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomSelected?.available, bedsType)
                list.forEach { bed ->
                    if (bed.number == bedSelected?.number) {
                        bed.userId = mobNumber
                        bed.occupied = true
                    }
                }
                roomSelected.available = Gson().toJson(list)
                roomSelected.createdby = prefmanager.userData?.UserName
                roomSelected.updatedon = currentdate()
                roomSelected.update = "update"
                Log.v("SELECTED_ROOM", Gson().toJson(roomSelected))
                addApartmentViewModel?.addRooms(roomSelected, success = {
                    showToast(this, "Tenant Added Successfully")
                    onBackPressed()
                }, error = {
                    showToast(this, it)
                })
            }
        }
    }


    private fun deleteTenant(tenant: TenantList, bedDeleted: (Boolean) -> Unit) {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    it.roomsList.filter { it.ID.toString() == tenant.roomno }.let { roomList ->
                        if (roomList.isNotEmpty()) {
                            val roomData = roomList.first()
                            if (roomData != null) {
                                deleteBedData(roomData, tenant, bedDeleted)
                            }
                        }
                    }
                }
            },
            error = {
                showToast(this, it)
            },
            apartmentid = tenant.apartmentId.toString(),
            floorno = tenant.floorno!!,
            flatno = tenant.flatno!!
        )

    }

    private fun deleteBedData(
        roomData: RoomData.RoomsList?,
        tenant: TenantList,
        bedDeleted: (Boolean) -> Unit
    ) {
        if (roomData?.available != null) {
            if (!roomData.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomData?.available, bedsType)
                list.forEach { bed ->
                    if (bed.userId == tenant.MobileNo) {
                        bed.userId = ""
                        bed.occupied = false
                    }
                }
                roomData.available = Gson().toJson(list)
                roomData.createdby = prefmanager.userData?.UserName
                roomData.updatedon = currentdate()
                roomData.update = "update"
                Log.v("SELECTED_ROOM", Gson().toJson(roomData))
                addApartmentViewModel?.addRooms(roomData, success = {
                    bedDeleted.invoke(true)
                }, error = {
                    showToast(this, it)
                })
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    private fun getFlats(selectFloor: String) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatList.clear()
                    it.flatList.add(
                        0,
                        FlatData.FlatList(ID = -1, flatname = "Select Flat")
                    )
                    flatList.addAll(it.flatList)
                    activitySwapBedsBinding.flatSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, flatList!!
                    )
                    activitySwapBedsBinding.flatSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectFlat = if (selectedItem != "Select Flat") {
                                    FlatSelected = flatList[p2]
                                    getRooms(FlatSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {

                }
            },
            error = {
                showToast(this, it)
            },
            prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentSelected?.ID.toString(),
            selectFloor
        )
    }

    private fun getRooms(flatSelected: FlatData.FlatList) {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomList.clear()
                    it.roomsList.add(
                        0,
                        RoomData.RoomsList(ID = -1, roomname = "Select Room")
                    )
                    roomList.addAll(it.roomsList)
                    activitySwapBedsBinding.roomSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, roomList!!
                    )
                    activitySwapBedsBinding.roomSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectRoom = if (selectedItem != "Select Room") {
                                    roomSelected = roomList[p2]
                                    showAvailableBeds(roomSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {

                }
            },
            error = {
                showToast(this, it)
            },
            apartmentid = apartmentSelected?.ID.toString(),
            floorno = selectFloor!!,
            flatno = flatSelected.ID.toString()
        )
    }

    private fun showAvailableBeds(roomSelected: RoomData.RoomsList) {
        if (roomSelected.available != null) {
            if (!roomSelected?.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomSelected?.available, bedsType)
                bedsList.clear()
                list.forEach { bed ->
                    if (bed.userId.isNullOrEmpty()) {
                        bedsList.add(bed)
                    }
                }
                bedsList.add(0, Beds(number = "Available Beds"))
                activitySwapBedsBinding.bedsSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, bedsList!!
                )
                activitySwapBedsBinding.bedsSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            val selectedItem = p0?.getItemAtPosition(p2).toString()
                            selectBed = if (selectedItem != "Available Beds") {
                                bedSelected = bedsList[p2]
//                                updatebeds(
//                                    roomSelected,
//                                    bedSelected!!,
//                                    activitySwapBedsBinding.editNumber.text.toString()
//                                )
                                selectedItem
                            } else ""
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }
        }
    }
}