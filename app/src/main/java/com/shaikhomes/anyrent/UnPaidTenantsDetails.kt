package com.shaikhomes.anyrent

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.databinding.ActivityUnpaidTenantsDetailsBinding
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.models.TenantData
import com.shaikhomes.anyrent.ui.models.TenantList
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.WhatsappAccessibilityService
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.isAccessibilityOn
import com.shaikhomes.anyrent.ui.utils.showToast
import java.net.URLEncoder

class UnPaidTenantsDetails : AppCompatActivity() {
    private lateinit var activityTenantDetailsBinding: ActivityUnpaidTenantsDetailsBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList: ArrayList<ApartmentList> = arrayListOf()
    private var tenantAdapter: TenantAdapter? = null
    private var tenantData: TenantData? = null
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantDetailsBinding = ActivityUnpaidTenantsDetailsBinding.inflate(layoutInflater)
        setContentView(activityTenantDetailsBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Tenants"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityTenantDetailsBinding.tenantList.apply {
            layoutManager = LinearLayoutManager(this@UnPaidTenantsDetails)
            tenantAdapter = TenantAdapter(this@UnPaidTenantsDetails, arrayListOf()).apply {
                setEditClickListener { tenantList ->
                    val intent = Intent(this@UnPaidTenantsDetails, TenantOverview::class.java)
                    intent.putExtra("tenant", Gson().toJson(tenantList))
                    startActivity(intent)
                    finish()
                }
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
                setDeleteClickListener { tenant ->
                    //deleteTenant(tenant)
                }
                setReminderClickListener { tenant ->
                    sendReminder(tenant)
                }
            }
            adapter = tenantAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        getPendingTenants()
    }

    private fun getPendingTenants() {
        if (prefmanager.selectedApartment != null && prefmanager.selectedApartment?.ID != null) {
            addApartmentViewModel?.getTenants(success = { tenantList ->
                getTotalDueTenents(prefmanager.selectedApartment?.ID.toString(), tenantList)
            }, error = {
                showToast(this, it)
            }, "", "", "", "due")
        }
    }

    private fun getTotalDueTenents(id: String, tenantData: TenantData) {
        if (tenantData.tenant_list.isNotEmpty()) {
            tenantData.tenant_list.filter { it.apartmentId == id }.let { tenantListData ->
                if (tenantListData.isNotEmpty()) {
                    val unPaidTenantData =
                        TenantData(tenant_list = tenantListData as ArrayList<TenantList>)
                    this.tenantData = unPaidTenantData
                    getApartments()
                }
            }
        }
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

    private fun deleteTenant(tenant: TenantList) {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    it.roomsList.filter { it.ID.toString() == tenant.roomno }.let { roomList ->
                        if (roomList.isNotEmpty()) {
                            showAlertDialog(roomList.first(), tenant)
                        }
                    }
                } else showAlertDialog(tenant = tenant)
            },
            error = {
                showToast(this, it)
            },
            apartmentid = tenant.apartmentId.toString(),
            floorno = tenant.floorno!!,
            flatno = tenant.flatno!!
        )
    }

    private fun showAlertDialog(roomData: RoomData.RoomsList? = null, tenant: TenantList) {
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to delete ${tenant.Name}?")
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                tenant.delete = "delete"
                addApartmentViewModel?.addTenant(tenant, success = {
                    if (it.status == "200") {
                        showToast(this@UnPaidTenantsDetails, "${tenant.Name} deleted successfully")
                        if (roomData != null) {
                            deleteBedData(roomData, tenant)
                        }
                    }
                }, error = {
                    showToast(this@UnPaidTenantsDetails, it)
                })
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

    private fun deleteBedData(roomData: RoomData.RoomsList?, tenant: TenantList) {
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
                    onBackPressed()
                }, error = {
                    showToast(this, it)
                })
            }
        }
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
        if (tenantData?.tenant_list?.isNotEmpty() == true) {
            tenantData?.tenant_list?.let { tenantAdapter?.updateList(it) }
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_broadcast).setVisible(true)
        menu.findItem(R.id.action_logout).setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_broadcast) {
            if (!isAccessibilityOn(this, WhatsappAccessibilityService::class.java)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                showBroadCast()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBroadCast() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.broadcast_dialog)
        dialog.setCancelable(true)
        val edtMsg = dialog.findViewById<AppCompatEditText>(R.id.edtMsg)
        val btnSend = dialog.findViewById<AppCompatButton>(R.id.btnSend)
        btnSend.setOnClickListener {
            if (edtMsg.text.toString().isNotEmpty()) {
                dialog.dismiss()
                sendBroadcastMessage(edtMsg.text.toString().trim())
            } else showToast(this, "Please enter message")
        }
        dialog.show()
    }

    private fun sendBroadcastMessage(message: String) {
        tenantAdapter?.getList()?.forEach { lead ->
            val i = Intent(Intent.ACTION_VIEW)
            try {
                /*${lead.contactnumber}*/
                val url =
                    "https://api.whatsapp.com/send?phone=${(if (!lead.countrycode.isNullOrEmpty()) lead.countrycode?.trim() else "+91") + lead.MobileNo?.trim()}" + "&text=" + URLEncoder.encode(
                        "${message} \uD83D\uDE0A",
                        "UTF-8"
                    )
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}