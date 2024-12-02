package com.shaikhomes.smartdiary

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityTenantOverviewBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.calculateDaysBetween
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
import com.shaikhomes.smartdiary.ui.utils.showToast
import java.lang.Math.abs
import java.net.URLEncoder
import java.util.Calendar


class TenantOverview : AppCompatActivity() {
    private lateinit var activityTenantOverviewBinding: ActivityTenantOverviewBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var tenantList: TenantList? = null
    private var addApartmentViewModel: AddApartmentViewModel? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantOverviewBinding = ActivityTenantOverviewBinding.inflate(layoutInflater)
        setContentView(activityTenantOverviewBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Tenant Details"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        tenantList =
            Gson().fromJson(intent.getStringExtra("tenant"), TenantList::class.java)
        activityTenantOverviewBinding.apply {
            tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    // Handle tab selected
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // Handle tab unselected
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // Handle tab reselected
                }
            })
            userName.text = tenantList?.Name
            userMobile.text = tenantList?.countrycode?.plus(tenantList?.MobileNo)
            profileImage.text = tenantList?.Name?.first().toString()
            checkin.text = "CheckIn Date : ${
                tenantList?.checkin?.dateFormat(
                    "dd-MM-yyyy 00:00:00",
                    "dd-MM-yyyy"
                )
            }"
            checkout.text = "CheckOut Date : ${
                tenantList?.checkout?.dateFormat(
                    "dd-MM-yyyy 00:00:00",
                    "dd-MM-yyyy"
                )
            }"
            room.text = tenantList?.details
            rent.text = "Per Day Rent AED ${tenantList?.rent}/-"
            floor.text = "Floor : ${tenantList?.floorno}"
            btnCall.setOnClickListener {
                try {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:" + "${(if (!tenantList?.countrycode.isNullOrEmpty()) tenantList?.countrycode else "+971") + tenantList?.MobileNo}")
                    )
                    startActivity(intent)
                } catch (exp: Exception) {
                    exp.printStackTrace()
                }
            }
            btnReminder.setOnClickListener {
                sendReminder(tenantList!!)
            }
            val checkOut = tenantList?.checkout?.dateFormat("dd-MM-yyyy 00:00:00", "dd-MM-yyyy")
            val currentDate = currentonlydate("dd-MM-yyyy")
            val rent =
                if (tenantList?.rent.isNullOrEmpty()) 0 else tenantList?.rent?.toInt()
            checkOut?.let {
                val days = calculateDaysBetween(currentDate, it)
                var totalRent = rent!! * days
                if (days < 0) {
                    dueLayout.visibility = View.VISIBLE
                    dueText.text =
                        "${tenantList?.Name} have a due of AED ${abs(totalRent)}/- \nSince ${checkOut}"
                    btnRecordPayment.setOnClickListener {
                        recordPayment(checkOut, totalRent)
                    }
                }
            }
        }
        getApartment(tenantList?.apartmentId)
        getFlat(tenantList?.apartmentId, tenantList?.floorno)
    }

    private fun recordPayment(checkOut: String, totalRent: Long) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.record_payment, null)
        val editCheckIn = view.findViewById<EditText>(R.id.editCheckIn)
        val editCheckOut = view.findViewById<EditText>(R.id.editCheckOut)
        val dueDate = view.findViewById<TextView>(R.id.dueDate)
        val updateButton = view.findViewById<Button>(R.id.updateButton)
        editCheckIn.setOnClickListener {
            selectCheckInDate(editCheckIn)
        }
        editCheckOut.setOnClickListener {
            selectCheckInDate(editCheckOut)
        }
        dueDate.text = "Due Since ${checkOut}"
        val safeClickListener = SafeClickListener {
            if (editCheckIn.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, "Please select checkin date", Toast.LENGTH_SHORT).show()
            } else if (editCheckOut.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, "Please select checkout date", Toast.LENGTH_SHORT).show()
            } else {
                tenantList?.checkin = editCheckIn.text.toString()
                tenantList?.checkout = editCheckOut.text.toString()
                tenantList?.checkout = editCheckOut.text.toString()
                tenantList?.UpdatedOn = currentdate()
                tenantList?.CreatedBy = prefmanager?.userData?.UserName
                tenantList?.duedate =
                    tenantList?.duedate?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.joinedon =
                    tenantList?.joinedon?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.update = "update"
                Log.v("TENANT_UPDATE", Gson().toJson(tenantList))
                addApartmentViewModel?.addTenant(tenantList!!, success = {
                    showToast(this, "Tenant Updated Successfully")
                    onBackPressed()
                }, error = {
                    showToast(this, it)
                })
            }
        }
        updateButton.setOnClickListener(safeClickListener)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
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

    private fun getApartment(apartmentid: String?) {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    val apartmentList = it.apartmentList.first()
                    activityTenantOverviewBinding.apply {
                        apartment.setText(
                            "Apartment : ${apartmentList?.apartmentname}"
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

    private fun getFlat(apartmentid: String?, floorNo: String?) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    val flatList = it.flatList.first()
                    activityTenantOverviewBinding.apply {
                        flat.setText(
                            "Flat No : ${flatList.flatname}"
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

    private fun sendReminder(tenant: TenantList) {
        val packageManager = packageManager
        val i = Intent(Intent.ACTION_VIEW)
        try {
            val url =
                "https://api.whatsapp.com/send?phone=${(if (!tenant?.countrycode.isNullOrEmpty()) tenant?.countrycode else "+971") + tenant?.MobileNo}" + "&text=" + URLEncoder.encode(
                    "This is reminder for your due for the rent",
                    "UTF-8"
                )
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }
}