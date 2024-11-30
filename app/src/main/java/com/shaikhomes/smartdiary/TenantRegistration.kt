package com.shaikhomes.smartdiary

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Html
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
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
//        if (validations()) {
//            val tenantList = TenantList(
//                Active = "1",
//                MobileNo = activityTenantsBinding.editNumber.text.toString(),
//                Name = activityTenantsBinding.editName.text.toString(),
//                apartmentId = apartmentSelected?.ID.toString(),
//                floorno = selectFloor,
//                roomno = roomSelected?.ID.toString(),
//                flatno = FlatSelected?.ID.toString(),
//                Gender = activityTenantsBinding.genderSpinner.selectedItem.toString(),
//                Profession = "",
//                rent = "",
//                rentstatus = "",
//                duedate = "",
//                paymentmode = "",
//                securitydeposit = "",
//                joinedon = currentdate(),
//                mailid = "",
//                ProofImageF = "",
//                ProofImageB = "",
//                CreatedBy = prefmanager.userData?.UserName,
//                UpdatedOn = currentdate(),
//                checkin = activityTenantsBinding.editCheckIn.text.toString(),
//                checkout = activityTenantsBinding.editCheckOut.text.toString(),
//                paid = "0",
//                total = "0",
//                countrycode = activityTenantsBinding.textDropDownChooseCountry.text.toString()
//            )
//            addApartmentViewModel?.addTenant(tenantList, success = {
//                updatebeds(
//                    roomSelected,
//                    bedSelected,
//                    activityTenantsBinding.editNumber.text.toString()
//                )
//            }, error = {
//                showToast(this, it)
//            })
//        }
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