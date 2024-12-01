package com.shaikhomes.smartdiary

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityTenantRegistrationBinding
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
    private var genderist = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantsBinding = ActivityTenantRegistrationBinding.inflate(layoutInflater)
        setContentView(activityTenantsBinding.root)
        supportActionBar?.title = "Tenants"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        roomsList =
            Gson().fromJson(intent.getStringExtra("ROOM_SELECT"), RoomData.RoomsList::class.java)
        selectedBed = Gson().fromJson(intent.getStringExtra("BED_SELECT"), Beds::class.java)
        getApartment(roomsList?.apartmentid)
        getFlat(roomsList?.apartmentid, roomsList?.floorno)
        activityTenantsBinding.apply {
            floor.setText(
                Html.fromHtml(
                    "Floor : <font color='#000E77'>${
                        roomsList?.floorno
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
            room.setText(
                Html.fromHtml(
                    "Room : <font color='#000E77'>${
                        roomsList?.roomname
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
            bed.setText(
                Html.fromHtml(
                    "Bed No : <font color='#000E77'>${
                        selectedBed?.number
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
        }
        genderist.add("male")
        genderist.add("female")
        activityTenantsBinding.genderSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, genderist!!
        )
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
        activityTenantsBinding.editCheckIn.setOnClickListener {
            selectCheckInDate(activityTenantsBinding.editCheckIn)
        }
        activityTenantsBinding.editCheckOut.setOnClickListener {
            selectcheckOutDate(activityTenantsBinding.editCheckOut)
        }
    }

    val safeClickListener = SafeClickListener {
        if (validations()) {
            val tenantList = TenantList(
                Active = "1",
                MobileNo = activityTenantsBinding.editNumber.text.toString(),
                Name = activityTenantsBinding.editName.text.toString(),
                apartmentId = roomsList?.apartmentid.toString(),
                floorno = roomsList?.floorno,
                roomno = roomsList?.ID?.toString(),
                flatno = roomsList?.flatno.toString(),
                Gender = activityTenantsBinding.genderSpinner.selectedItem.toString(),
                Profession = "",
                rent = roomsList?.rentperday,
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
                countrycode = activityTenantsBinding.textDropDownChooseCountry.text.toString(),
                details = "${roomsList?.roomname} - B${selectedBed?.number}"
            )
            addApartmentViewModel?.addTenant(tenantList, success = {
                updatebeds(
                    roomsList,
                    selectedBed,
                    activityTenantsBinding.editNumber.text.toString()
                )
            }, error = {
                showToast(this, it)
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

    private fun validations(): Boolean {
        var flag = true
        if (activityTenantsBinding.editName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckIn.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckIn Date", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckOut.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckOut Date", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    private fun getApartment(apartmentid: String?) {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList = it.apartmentList.first()
                    activityTenantsBinding.apply {
                        apartment.setText(
                            Html.fromHtml(
                                "Apartment : <font color='#000E77'>${
                                    apartmentList?.apartmentname
                                }</font>"
                            ),
                            TextView.BufferType.SPANNABLE
                        )
                    }
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentid!!
        )
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

    private fun getFlat(apartmentid: String?, floorNo: String?) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatList = it.flatList.first()
                    activityTenantsBinding.apply {
                        flat.setText(
                            Html.fromHtml(
                                "Flat : <font color='#000E77'>${
                                    flatList?.flatname
                                }</font>"
                            ),
                            TextView.BufferType.SPANNABLE
                        )
                    }
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