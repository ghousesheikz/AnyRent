package com.shaikhomes.smartdiary.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.databinding.FragmentHomeBinding
import com.shaikhomes.smartdiary.ExpensesList
import com.shaikhomes.smartdiary.RoomsAvailableActivity
import com.shaikhomes.smartdiary.UnPaidTenantsDetails
import com.shaikhomes.smartdiary.ui.PropertyActivity
import com.shaikhomes.smartdiary.ui.models.ApartmentData
import com.shaikhomes.smartdiary.ui.models.Beds
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.models.TenantData
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.utils.ImagePicker
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.calculateDaysBetween
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
import com.shaikhomes.smartdiary.ui.utils.getExpensesList
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var leadAdapter: LeadAdapter? = null
    var homeViewModel: HomeViewModel? = null
    var assignTo: String = ""
    var propertyData: PropertyData? = null
    var apartmentData: ApartmentData? = null
    private val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.propertyLayout.setOnClickListener {
            startActivity(Intent(requireContext(), PropertyActivity::class.java))
        }
        binding.goBtn.setOnClickListener {
            startActivity(Intent(requireContext(), PropertyActivity::class.java))
        }
        cashProgress(0f, 1f)
        val root: View = binding.root
        return root
    }

    fun cashProgress(completed: Float, incomplete: Float) {
        val completedWeight = 0.3f // 30% completed
        val remainingWeight = 1 - completedWeight // 70% incomplete
        val completedParams =
            LinearLayout.LayoutParams(0, 50, completed)
        val remainingParams =
            LinearLayout.LayoutParams(0, 50, incomplete)
        val completedView = binding.progressBarLayout.getChildAt(0)
        binding.completedView.layoutParams = completedParams
        val remainingView = binding.progressBarLayout.getChildAt(1)
        binding.remainingView.layoutParams = remainingParams
        binding.bedsLayout.setOnClickListener {
            if (prefmanager.selectedApartment != null) {
                requireActivity().startActivity(
                    Intent(
                        requireContext(),
                        RoomsAvailableActivity::class.java
                    )
                )
            }
        }
        binding.currentMonth.text = currentonlydate("MMMM")
    }

    override fun onResume() {
        super.onResume()
        getApartments()
        getPendingTenants()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ImagePicker.isNotificationsPermissionRequired(requireContext(), this)
        }
        getExpenses()
    }

    private fun getExpenses() {
        homeViewModel?.getExpenses(success = { expensesData ->
            CoroutineScope(Dispatchers.IO).launch {
                if (!expensesData.expensesList.isNullOrEmpty()) {
                    val todaysExpenses =
                        getExpensesList(expensesData.expensesList, currentDay = true)
                    var amount = 0
                    todaysExpenses.forEach { records->
                        if(!records.creditAmount.isNullOrEmpty()) amount += records.creditAmount?.toInt()!!
                        if(!records.debitAmount.isNullOrEmpty()) amount -= records.debitAmount?.toInt()!!
                    }
                    withContext(Dispatchers.Main){
                        binding.txtExpense.setText("AED ${amount}/-")
                        binding.expenseLayout.setOnClickListener {
                            startActivity(Intent(requireContext(), ExpensesList::class.java))
                        }
                    }
                }else {
                    withContext(Dispatchers.Main){
                        binding.txtExpense.setText("AED 0/-")
                    }
                }
            }
        }, error = {}, apartmentid = prefmanager.selectedApartment?.ID.toString(), "", "")
    }

    private fun getApartments() {
        homeViewModel?.getApartments(success = {
            if (it.apartmentList.isNotEmpty()) {
                apartmentData = it
                if (prefmanager.selectedApartment == null) {
                    prefmanager.selectedApartment = apartmentData?.apartmentList?.first()
                    binding.apartmentName.apply {
                        text = prefmanager.selectedApartment?.apartmentname
                    }
                    binding.apartmentAddress.apply {
                        text = prefmanager.selectedApartment?.address
                    }
                } else {
                    binding.apartmentName.apply {
                        text = prefmanager.selectedApartment?.apartmentname
                    }
                    binding.apartmentAddress.apply {
                        text = prefmanager.selectedApartment?.address
                    }
                }
                bindData()
                getPendingTenants()
            }
        }, error = {
            showToast(requireContext(), it)
        }, userid = prefmanager.userData?.UserId.toString(), apartmentid = "")
    }

    private fun getPendingTenants() {
        if (prefmanager.selectedApartment != null && prefmanager.selectedApartment?.ID != null) {
            homeViewModel?.getTenants(success = { tenantList ->
                getTotalDueTenents(prefmanager.selectedApartment?.ID.toString(), tenantList)
            }, error = {
                showToast(requireContext(), it)
            }, "", "", "", "due")
        }
    }

    private fun getTotalDueTenents(id: String, tenantData: TenantData) {
        if (tenantData.tenant_list.isNotEmpty()) {
            tenantData.tenant_list.filter { it.apartmentId == id }.let { tenantListData ->
                if (tenantListData.isNotEmpty()) {
                    binding.txtTenantsCount.text = tenantListData.size.toString()
                    bindPendingAmount(tenantListData)
                    val unPaidTenantData =
                        TenantData(tenant_list = tenantListData as ArrayList<TenantList>)
                    binding.unpaidLayout.setOnClickListener {
                        val intent = Intent(requireContext(), UnPaidTenantsDetails::class.java)
                        intent.putExtra("UNPAID_TENANTS", Gson().toJson(unPaidTenantData))
                        startActivity(intent)
                    }
                } else binding.txtTenantsCount.text = "0"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindPendingAmount(tenantListData: List<TenantList>) {
        lifecycleScope.launch {
            var pendingAmt: Long = 0
            tenantListData.forEach { tenantList ->
                val checkOut = tenantList.checkout?.dateFormat("dd-MM-yyyy 00:00:00", "dd-MM-yyyy")
                val currentDate = currentonlydate("dd-MM-yyyy")
                val rent =
                    if (tenantList.rent.isNullOrEmpty()) 0 else tenantList.rent?.toInt()
                checkOut?.let {
                    val days = calculateDaysBetween(currentDate, it)
                    val totalRent = rent!! * days
                    pendingAmt += kotlin.math.abs(totalRent)
                }
            }
            binding.pendingAmt.text = "AED ${pendingAmt}/-"
            binding.totalAmt.text = "AED ${pendingAmt}/-"
        }
    }

    private fun bindData() {
        if (prefmanager.selectedApartment != null) {
            homeViewModel?.getRooms(
                success = {
                    bindRoomData(it.roomsList)
                },
                error = {
                    showToast(requireContext(), it)
                },
                apartmentid = prefmanager.selectedApartment?.ID.toString(),
                floorno = "",
                flatno = ""
            )
        }
    }

    private fun bindRoomData(roomsList: ArrayList<RoomData.RoomsList>) {
        lifecycleScope.launch {
            if (roomsList.isNotEmpty()) {
                roomsList.getCapacity { available, occupied ->
                    val tot = available + occupied
                    binding.txtBedsCount.text = "${available} / ${tot}"
                }
            } else binding.txtBedsCount.text = "0 / 0"
        }
    }

    suspend fun ArrayList<RoomData.RoomsList>.getCapacity(capacity: (Int, Int) -> Unit) {
        var available: Int = 0
        var occupied: Int = 0
        this.forEach {
            if (it?.available != null) {
                if (!it.available.isNullOrEmpty()) {
                    val list: ArrayList<Beds> = Gson().fromJson(it?.available, bedsType)
                    list.forEach { bed ->
                        if (bed.userId.isNullOrEmpty()) {
                            available += 1
                        } else occupied += 1
                    }
                }
            }
        }
        capacity.invoke(available, occupied)
    }
}


