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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.Gson
import com.kevinschildhorn.otpview.OTPView
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityTenantOverviewBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.calculateCharge
import com.shaikhomes.smartdiary.ui.utils.calculateDaysBetween
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
import com.shaikhomes.smartdiary.ui.utils.getFutureDate
import com.shaikhomes.smartdiary.ui.utils.makeCamelCase
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
            if (!tenantList?.userImage.isNullOrEmpty()) {
                circularImageView.visibility = View.VISIBLE
                profileImage.visibility = View.GONE
                Glide.with(this@TenantOverview)
                    .load(tenantList?.userImage)
                    .transform(CircleTransformation()) // Apply custom circle transformation
                    .into(circularImageView)
            } else {
                circularImageView.visibility = View.GONE
                profileImage.visibility = View.VISIBLE
                profileImage.text = tenantList?.Name?.first().toString()
            }
            checkin.text = "CheckIn Date : ${
                tenantList?.checkin?.dateFormat(
                    "dd-MM-yyyy 00:00:00",
                    "dd-MM-yyyy"
                )
            }"
            joinedOn.text = "Joined On : ${
                tenantList?.joinedon?.dateFormat(
                    "dd-MM-yyyy hh:mm:ss",
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
            securityDeposit.text =
                "Security Deposit AED ${if (tenantList?.securitydeposit.isNullOrEmpty()) "0" else tenantList?.securitydeposit}/-"
            floor.text = "Floor : ${tenantList?.floorno}"
            rentType.text = "Rent Type : ${tenantList?.renttype.makeCamelCase()}"
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
                if (tenantList?.rent.isNullOrEmpty()) 0.0 else tenantList?.rent?.toDouble()
            checkOut?.let {
                val days = calculateDaysBetween(currentDate, it)
                var penality = 0.0
                var totalRent = if (tenantList?.renttype == "monthly") {
                    penality = calculateCharge(rent!!, 0.1) * days
                    penality = kotlin.math.abs(penality)
                    rent + penality
                } else rent!! * days
                if (days < 0 || tenantList?.rentstatus == "pending") {
                    dueLayout.visibility = View.VISIBLE
                    if (tenantList?.rentstatus == "pending") {
                        val paidAmt =
                            if (tenantList?.paid.isNullOrEmpty()) 0 else (tenantList?.paid
                                ?: "0").toInt()
                        val dueAmt = totalRent - paidAmt
                        dueText.text =
                            "${tenantList?.Name} have a due of AED ${abs(dueAmt)}/-  Paid Amount AED ${paidAmt}/- Total Amount AED ${totalRent}/-"
                    } else {
                        dueText.text =
                            "${tenantList?.Name} have a due of AED ${abs(totalRent)}/-  with penality AED ${penality}/-\nSince ${checkOut}"
                    }
                    btnRecordPayment.setOnClickListener {
                        val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
                        val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
                        AlertDialog.Builder(this@TenantOverview).apply {
                            this.setMessage("Do you want to change checkin checkout dates?")
                            this.setView(dialogView)
                            this.setPositiveButton(
                                "YES"
                            ) { p0, p1 ->
                                if (otpView.getStringFromFields() == "278692") {
                                    recordPayment(checkOut, totalRent)
                                } else showToast(this@TenantOverview, "Incorrect OTP")
                            }
                            this.setNegativeButton(
                                "NO"
                            ) { p0, p1 ->
                                p0.dismiss()
                            }
                            this.setCancelable(true)
                            this.show()
                        }

                    }
                }
                btnChangeDates.setOnClickListener {
                    val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
                    val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
                    AlertDialog.Builder(this@TenantOverview).apply {
                        this.setMessage("Do you want to change checkin checkout dates?")
                        this.setView(dialogView)
                        this.setPositiveButton(
                            "YES"
                        ) { p0, p1 ->
                            if (otpView.getStringFromFields() == "278692") {
                                recordPayment(checkOut, totalRent)
                            } else showToast(this@TenantOverview, "Incorrect OTP")
                        }
                        this.setNegativeButton(
                            "NO"
                        ) { p0, p1 ->
                            p0.dismiss()
                        }
                        this.setCancelable(true)
                        this.show()
                    }
                }
            }
            btnEdit.setOnClickListener {
                val intent = Intent(this@TenantOverview, EditTenant::class.java)
                intent.putExtra("edit_tenant", Gson().toJson(tenantList))
                startActivity(intent)
                finish()
            }
        }
        getApartment(tenantList?.apartmentId)
        getFlat(tenantList?.apartmentId, tenantList?.floorno)
    }

    private fun recordPayment(checkOut: String, totalRent: Double) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.record_payment, null)
        val editCheckIn = view.findViewById<EditText>(R.id.editCheckIn)
        val editCheckOut = view.findViewById<EditText>(R.id.editCheckOut)
        val editCheckOutDays = view.findViewById<EditText>(R.id.editCheckOutDays)
        val editAmountReceived = view.findViewById<EditText>(R.id.editAmountReceived)
        val editDueDate = view.findViewById<EditText>(R.id.editDueDate)
        val dueDate = view.findViewById<TextView>(R.id.dueDate)
        val updateButton = view.findViewById<Button>(R.id.updateButton)
//        editCheckIn.setOnClickListener {
//            selectCheckInDate(editCheckIn)
//        }
        editCheckIn.setText(tenantList?.checkin?.dateFormat("dd-MM-yyyy hh:mm:ss", "dd-MM-yyyy"))
        editCheckOutDays.doAfterTextChanged {
            if (it.toString().isNotEmpty()) {
                val number = it.toString().toInt()
                if (number <= 0) {
                    editCheckOut.setText("")
                } else editCheckOut.setText(getFutureDate(checkOut, number))
            } else editCheckOut.setText("")
        }
        if (tenantList?.renttype == "monthly") {
            editCheckOutDays.setText("30")
            editCheckOutDays.isClickable = false
            editCheckOutDays.isEnabled = false
            if (tenantList?.rentstatus == "pending") {
                dueDate.visibility = View.INVISIBLE
                editAmountReceived.setText("0")
                editCheckOut.setText(checkOut)
            } else {
                editAmountReceived.setText(tenantList?.rent)
            }
            editAmountReceived.doAfterTextChanged {
                if (it.toString().isNotEmpty()) {
                    val number = it.toString().toInt()
                    val rent = tenantList?.rent?.toInt()
                    val pending = (tenantList?.paid?:"0").toInt() + number
                    if (number < 0) {
                        editAmountReceived.setText("0")
                    } else if (number > rent!!) {
                        showToast(this,"Amount Exceeded")
                        editAmountReceived.setText(tenantList?.rent)
                    } else if (pending > rent!!) {
                        showToast(this,"Amount Exceeded")
                        editAmountReceived.setText("0")
                    } else {
                        val rentPerDay = rent / 30
                        val daysCovered = number / rentPerDay
                        Log.v("DAYS_COVERED", daysCovered.toString())
                        //editDueDate.setText(getFutureDate(checkOut, daysCovered))
                    }
                }
            }
        } else {
            editAmountReceived.visibility = View.GONE
            editAmountReceived.setText("0")
        }
        dueDate.text = "Due Since ${checkOut}"
        val safeClickListener = SafeClickListener {
            if (editCheckIn.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, "Please select checkin date", Toast.LENGTH_SHORT).show()
            } else if (editCheckOut.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, "Please select checkout date", Toast.LENGTH_SHORT).show()
            } else if (editAmountReceived.text.toString()
                    .isNullOrEmpty() || editAmountReceived.text.toString() == "0" && tenantList?.renttype == "monthly"
            ) {
                Toast.makeText(this, "Please enter received amount", Toast.LENGTH_SHORT).show()
            } else {
                val amtReceived = if(tenantList?.rentstatus=="pending") (editAmountReceived.text.toString().toInt()+ (tenantList?.paid?:"0").toInt()) else  editAmountReceived.text.toString().toInt()
                val rent = tenantList?.rent?.toInt()
                var status = ""
                if (amtReceived == rent || tenantList?.renttype != "monthly") {
                    status = "completed"
                } else status = "pending"
                tenantList?.checkin =
                    editCheckIn.text.toString().dateFormat("dd-MM-yyyy", "yyyy-MM-dd")
                tenantList?.checkout =
                    editCheckOut.text.toString().dateFormat("dd-MM-yyyy", "yyyy-MM-dd")
                tenantList?.UpdatedOn = currentdate()
                tenantList?.CreatedBy = prefmanager?.userData?.UserName
                tenantList?.duedate =
                    tenantList?.duedate?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.joinedon =
                    tenantList?.joinedon?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.update = "update"
                tenantList?.rentstatus = status
                tenantList?.paid = amtReceived.toString()
                //Log.v("TENANT_UPDATE", Gson().toJson(tenantList))
                addApartmentViewModel?.addTenant(tenantList!!, success = {
                    bottomSheetDialog.dismiss()
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