package com.shaikhomes.smartdiary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shaikhomes.anyrent.databinding.ActivityTenantDetailsBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast

class TenantDetailsActivity : AppCompatActivity() {
    private lateinit var activityTenantDetailsBinding: ActivityTenantDetailsBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList: ArrayList<ApartmentList> = arrayListOf()
    private var tenantAdapter: TenantAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantDetailsBinding = ActivityTenantDetailsBinding.inflate(layoutInflater)
        setContentView(activityTenantDetailsBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Tenants"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityTenantDetailsBinding.tenantList.apply {
            layoutManager = LinearLayoutManager(this@TenantDetailsActivity)
            tenantAdapter = TenantAdapter(this@TenantDetailsActivity, arrayListOf()).apply {
                setApartmentClickListener { tenantList, apartment->
                    if (apartmentList.isNotEmpty()) {
                        apartmentList.filter { it.ID.toString() == tenantList.apartmentId }
                            .let { apartmentList ->
                                if (apartmentList.isNotEmpty()) {
                                    apartment.text = apartmentList.first().apartmentname
                                }
                            }
                    }
                }
                setCallClickListener {tenant->
                    try {
                        val intent = Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:" + tenant.MobileNo)
                        )
                        context.startActivity(intent)
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
                }
                setDeleteClickListener {tenant->
                    deleteTenant(tenant)
                }
                setReminderClickListener {

                }
            }
            adapter = tenantAdapter
        }
        getApartments()
    }

    private fun deleteTenant(tenant: TenantList) {
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to delete ${tenant.Name}?")
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                tenant.delete = "delete"
                addApartmentViewModel?.addTenant(tenant, success = {
                    if (it.status == "200") {
                        showToast(this@TenantDetailsActivity, "${tenant.Name} deleted successfully")
                        getApartments()
                    }
                }, error = {
                    showToast(this@TenantDetailsActivity, it)
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
        addApartmentViewModel?.getTenants(success = {
            tenantAdapter?.updateList(it.tenant_list)
        }, error = {
            showToast(this, it)
        }, mobileNo = "", apartmentid = "", active = "","")
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }
}