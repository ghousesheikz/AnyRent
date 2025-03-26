package com.shaikhomes.anyrent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kevinschildhorn.otpview.OTPView
import com.shaikhomes.anyrent.databinding.ActivityTenantApprovalBinding
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.dateFormat
import com.shaikhomes.anyrent.ui.utils.showToast

class TenantApproval : AppCompatActivity() {
    private lateinit var activityTenantApprovalBinding: ActivityTenantApprovalBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList: ArrayList<ApartmentList> = arrayListOf()
    private var tenantAdapter: TenantApprovalAdapter? = null
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantApprovalBinding = ActivityTenantApprovalBinding.inflate(layoutInflater)
        setContentView(activityTenantApprovalBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Tenant Approval"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityTenantApprovalBinding.tenantList.apply {
            layoutManager = LinearLayoutManager(this@TenantApproval)
            tenantAdapter = TenantApprovalAdapter(this@TenantApproval, arrayListOf()).apply {
                setApartmentClickListener { tenantList, apartment ->
                    if (apartmentList.isNotEmpty()) {
                        apartmentList.filter { it.ID.toString() == tenantList.apartmentId }
                            .let { apartmentList ->
                                if (apartmentList.isNotEmpty()) {
                                    if (tenantList.details.isNullOrEmpty()) {
                                        apartment.text = apartmentList.first().apartmentname
                                    } else apartment.text =
                                        apartmentList.first().apartmentname.plus(" - ${tenantList.details}")
                                }
                            }
                    }
                }
                setCallClickListener { tenant ->
                    try {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:" + "${(if (!tenant?.countrycode.isNullOrEmpty()) tenant?.countrycode else "+971") + tenant?.MobileNo}")
                        )
                        context.startActivity(intent)
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
                }
                setApprovalClickListener { tenant ->
                    approveTenant(tenant)
                }
                setDeleteClickListener { tenant ->
                    deleteTenant(tenant)
                }
            }
            adapter = tenantAdapter
        }
        getApartments()
    }

    private fun approveTenant(tenant: TenantList) {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    it.roomsList.filter { it.ID.toString() == tenant.roomno }.let { roomList ->
                        if (roomList.isNotEmpty()) {
                            showApproveDialog(roomList.first(), tenant)
                        }
                    }
                } else showToast(this, "Given room details are invalid")
            },
            error = {
                showToast(this, it)
            },
            apartmentid = tenant.apartmentId.toString(),
            floorno = tenant.floorno!!,
            flatno = tenant.flatno!!
        )
    }

    private fun getApartments() {
        addApartmentViewModel?.getApartments(success = {
            apartmentList.clear()
            apartmentList.addAll(it.apartmentList)
            getTenants()
        }, error = {
            showToast(this, it)
        }, userid = "", apartmentid = "")
    }

    private fun getTenants() {
        addApartmentViewModel?.getTenants(
            success = {
                it.tenant_list.filter { it.apartmentId == prefmanager.selectedApartment?.ID.toString() }
                    .let { tenantListData ->
                        try {
                            /*
                                                        val sortedList = tenantListData.sortedWith(compareBy(
                                                            {
                                                                it.details?.toUpperCase(Locale.ROOT)?.substringBefore("-")
                                                                    ?.trim()
                                                            }, // Sort by room number
                                                            {
                                                                it.details?.toUpperCase(Locale.ROOT)?.substringAfter("B")
                                                                    ?.toInt()
                                                            } // Sort by bed number
                                                        ))*/
                            // it.tenant_list.sortByDescending { it.details }
                            tenantAdapter?.updateList(tenantListData)
                        } catch (exp: Exception) {
                            tenantAdapter?.updateList(tenantListData)
//                            tenantListData.sortedByDescending { it.details }
//                            tenantAdapter?.updateList(tenantListData.reversed())
                        }
                    }
            },
            error = {
                showToast(this, it)
            },
            mobileNo = "",
            apartmentid = "",
            active = "0",
            ""
        )
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    private fun deleteTenant(tenant: TenantList) {
        showAlertDialog(tenant)
    }

    private fun showAlertDialog(tenant: TenantList) {
        val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
        val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to delete ${tenant.Name}?")
            this.setView(dialogView)
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                if (otpView.getStringFromFields() == "009289") {
                    tenant.delete = "delete"
                    addApartmentViewModel?.addTenant(tenant, success = {
                        if (it.status == "200") {
                            showToast(
                                this@TenantApproval,
                                "${tenant.Name} deleted successfully"
                            )
                            getApartments()
                        }
                    }, error = {
                        showToast(this@TenantApproval, it)
                    })
                } else showToast(this@TenantApproval, "Incorrect OTP")
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

    private fun showApproveDialog(roomData: RoomData.RoomsList? = null, tenant: TenantList) {
        val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
        val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to Approve ${tenant.Name}?")
            this.setView(dialogView)
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                if (otpView.getStringFromFields() == "778899") {
                    if (roomData != null) {
                        deleteBedData(roomData, tenant, { inserted ->
                            if (inserted) {
                                tenant.Active = "1"
                                tenant.checkin =
                                    tenant.checkin?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                                tenant.checkout =
                                    tenant.checkout?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                                tenant.UpdatedOn = currentdate()
                                tenant.CreatedBy = prefmanager?.userData?.UserName
                                tenant.duedate =
                                    tenant.duedate?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                                tenant.joinedon =
                                    tenant.joinedon?.dateFormat("MM/dd/yyyy hh:mm:ss aa", "yyyy-MM-dd")
                                tenant.update = "update"
                                tenant.paid = "0"
                                addApartmentViewModel?.addTenant(tenant, success = {
                                    if (it.status == "200") {
                                        showToast(
                                            this@TenantApproval,
                                            "${tenant.Name} Approved successfully"
                                        )
                                        getApartments()
                                        /*if (roomData != null) {
                                            deleteBedData(roomData, tenant)
                                        }*/
                                    }
                                }, error = {
                                    showToast(this@TenantApproval, it)
                                })
                            } else showToast(this@TenantApproval, "Incorrect OTP")

                        })
                    }
                }
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

    private fun deleteBedData(
        roomData: RoomData.RoomsList?,
        tenant: TenantList,
        isTenantInserted: (Boolean) -> Unit
    ) {
        var isInserted: Boolean = false
        if (roomData?.available != null) {
            if (!roomData.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomData?.available, bedsType)
                list.forEach { bed ->
                    if (bed.number == tenant.getBedNo()) {
                        if (bed.userId.isNullOrEmpty()) {
                            isInserted = true
                            bed.userId = tenant.MobileNo
                            bed.occupied = true
                        }
                    }
                }
                roomData.available = Gson().toJson(list)
                roomData.createdby = prefmanager.userData?.UserName
                roomData.updatedon = currentdate()
                roomData.update = "update"
                Log.v("SELECTED_ROOM", Gson().toJson(roomData))
                if (isInserted) {
                    isTenantInserted.invoke(true)
                    addApartmentViewModel?.addRooms(roomData, success = {

                    }, error = {
                        showToast(this, it)
                    })
                } else {
                    isTenantInserted.invoke(false)
                    showToast(this, "Bed already occupied")
                }
            }
        }
    }

    fun TenantList.getBedNo(): String {
        if (this.details.isNullOrEmpty()) return ""
        try {
            val regex = "B(\\d+)".toRegex()
            return regex.find(this.details!!)?.groupValues?.get(1) ?: ""
        } catch (exp: Exception) {
            return ""
        }
    }
}