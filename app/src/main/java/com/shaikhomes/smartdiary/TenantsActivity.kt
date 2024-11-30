package com.shaikhomes.smartdiary

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityTenantsBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.Beds
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.getCountryList
import com.shaikhomes.smartdiary.ui.utils.showToast
import java.util.Calendar

class TenantsActivity : AppCompatActivity() {

    private lateinit var activityTenantsBinding: ActivityTenantsBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    val type = object : TypeToken<ArrayList<String>>() {}.type
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList = arrayListOf<ApartmentList>()
    private var floorList = arrayListOf<String>()
    private var flatList = arrayListOf<FlatData.FlatList>()
    private var roomList = arrayListOf<RoomData.RoomsList>()
    private var genderist = arrayListOf<String>()
    private var bedsList = arrayListOf<Beds>()
    private var apartmentSelected: ApartmentList? = null
    private var bedSelected: Beds? = null
    private var FlatSelected: FlatData.FlatList? = null
    private var roomSelected: RoomData.RoomsList? = null
    private var selectApart: String? = null
    private var selectFloor: String? = null
    private var selectFlat: String? = null
    private var selectRoom: String? = null
    private var selectBed: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantsBinding = ActivityTenantsBinding.inflate(layoutInflater)
        setContentView(activityTenantsBinding.root)
        supportActionBar?.title = "Tenants"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        apartmentList.add(ApartmentList(ID = -1, apartmentname = "Select Apartment"))
        floorList.add("Select Floor")
        flatList.add(FlatData.FlatList(ID = -1, flatname = "Select Flat"))
        roomList.add(RoomData.RoomsList(ID = -1, roomname = "Select Room"))
        genderist.add("male")
        genderist.add("female")
        bedsList.add(Beds(number = "Available Beds"))
        activityTenantsBinding.apartmentSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, apartmentList!!
        )
        activityTenantsBinding.apartmentSpinner.onItemSelectedListener =
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
        activityTenantsBinding.floorSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, floorList!!
        )
        activityTenantsBinding.floorSpinner.onItemSelectedListener =
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
        activityTenantsBinding.flatSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, flatList!!
        )
        activityTenantsBinding.flatSpinner.onItemSelectedListener =
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
        activityTenantsBinding.roomSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, roomList!!
        )
        activityTenantsBinding.roomSpinner.onItemSelectedListener =
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
        activityTenantsBinding.genderSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, genderist!!
        )
        activityTenantsBinding.bedsSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, bedsList!!
        )
        activityTenantsBinding.bedsSpinner.onItemSelectedListener =
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
        activityTenantsBinding.editCheckIn.setOnClickListener {
            selectCheckInDate(activityTenantsBinding.editCheckIn)
        }
        activityTenantsBinding.editCheckOut.setOnClickListener {
            selectcheckOutDate(activityTenantsBinding.editCheckOut)
        }
        getApartments()
        activityTenantsBinding.addTenant.setOnClickListener(safeClickListener)
        activityTenantsBinding.textDropDownChooseCountry.setOnClickListener {
            try {
                val dialog =
                    CountrySelectionDialog.newInstance(
                        listener = { selectedCountry ->
                            activityTenantsBinding.textDropDownChooseCountry.text =
                                selectedCountry.dial_code
                        },
                        list = getCountryList(this)
                    )
                try {
                    val fragmentTransaction =
                        supportFragmentManager?.beginTransaction()
                    fragmentTransaction?.add(dialog, null)
                    fragmentTransaction?.commitAllowingStateLoss()
                } catch (exception: IllegalStateException) {
                    // do nothing
                }
            } catch (exception: IllegalStateException) {
                // do nothing
            }
        }
    }

    val safeClickListener = SafeClickListener {
        if (validations()) {
            val tenantList = TenantList(
                Active = "1",
                MobileNo = activityTenantsBinding.editNumber.text.toString(),
                Name = activityTenantsBinding.editName.text.toString(),
                apartmentId = apartmentSelected?.ID.toString(),
                floorno = selectFloor,
                roomno = roomSelected?.ID.toString(),
                flatno = FlatSelected?.ID.toString(),
                Gender = activityTenantsBinding.genderSpinner.selectedItem.toString(),
                Profession = "",
                rent = "",
                rentstatus = "",
                duedate = "",
                paymentmode = "",
                securitydeposit = "",
                joinedon = currentdate(),
                mailid = "",
                ProofImageF = "",
                ProofImageB = "",
                CreatedBy = prefmanager.userData?.UserName,
                UpdatedOn = currentdate(),
                checkin = activityTenantsBinding.editCheckIn.text.toString(),
                checkout = activityTenantsBinding.editCheckOut.text.toString(),
                paid = "0",
                total = "0",
                countrycode = activityTenantsBinding.textDropDownChooseCountry.text.toString()
            )
            addApartmentViewModel?.addTenant(tenantList, success = {
                updatebeds(
                    roomSelected,
                    bedSelected,
                    activityTenantsBinding.editNumber.text.toString()
                )
            }, error = {
                showToast(this, it)
            })
        }
    }

    private fun validations(): Boolean {
        var flag = true
        if (activityTenantsBinding.editName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
        } else if (selectApart?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Apartment", Toast.LENGTH_SHORT).show()
        } else if (selectFloor?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Floor", Toast.LENGTH_SHORT).show()
        } else if (selectFlat?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Flat", Toast.LENGTH_SHORT).show()
        } else if (selectRoom?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Room", Toast.LENGTH_SHORT).show()
        } else if (selectBed?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Allocate Bed", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckIn.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckIn Date", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckOut.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckOut Date", Toast.LENGTH_SHORT).show()
        }
        return flag
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
                    activityTenantsBinding.apartmentSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, apartmentList!!
                    )
                    activityTenantsBinding.apartmentSpinner.onItemSelectedListener =
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
                activityTenantsBinding.floorSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, floorList!!
                )
                activityTenantsBinding.floorSpinner.onItemSelectedListener =
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
                    activityTenantsBinding.flatSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, flatList!!
                    )
                    activityTenantsBinding.flatSpinner.onItemSelectedListener =
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
                    activityTenantsBinding.roomSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, roomList!!
                    )
                    activityTenantsBinding.roomSpinner.onItemSelectedListener =
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
                activityTenantsBinding.bedsSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, bedsList!!
                )
                activityTenantsBinding.bedsSpinner.onItemSelectedListener =
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
//                                    activityTenantsBinding.editNumber.text.toString()
//                                )
                                selectedItem
                            } else ""
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    private fun selectCheckInDate(checkIn: EditText) {
        var calendar = Calendar.getInstance()
        val datePickerDialog = this.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    checkIn.setText("$year-${monthOfYear.plus(1)}-$dayOfMonth")

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        // datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()
    }

    private fun selectcheckOutDate(checkOut: EditText) {
        var calendar = Calendar.getInstance()
        val datePickerDialog = this.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    checkOut.setText("$year-${monthOfYear.plus(1)}-$dayOfMonth")

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        //datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()
    }
}