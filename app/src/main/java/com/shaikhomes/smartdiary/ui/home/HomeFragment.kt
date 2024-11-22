package com.shaikhomes.smartdiary.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.shaikhomes.anyrent.databinding.FragmentHomeBinding
import com.shaikhomes.smartdiary.ui.PropertyActivity
import com.shaikhomes.smartdiary.ui.models.ApartmentData
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.launch


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
        cashProgress(0f,1f)
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
    }

    override fun onResume() {
        super.onResume()
        getApartments()
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
                } else {
                    binding.apartmentName.apply {
                        text = prefmanager.selectedApartment?.apartmentname
                    }
                }
                bindData()
            }
        }, error = {
            showToast(requireContext(), it)
        }, userid = prefmanager.userData?.UserId.toString(), apartmentid = "")
    }

    private fun bindData() {
        if(prefmanager.selectedApartment!=null){
            homeViewModel?.getRooms(success = {
                bindRoomData(it.roomsList)
            }, error = {
                showToast(requireContext(), it)
            }, apartmentid = prefmanager.selectedApartment?.ID.toString(), floorno = "", flatno = "")
        }
    }

    private fun bindRoomData(roomsList: ArrayList<RoomData.RoomsList>) {
        lifecycleScope.launch {
            if (roomsList.isNotEmpty()) {
                roomsList.getCapacity { tot->
                    binding.txtBedsCount.text = "${tot} / ${tot}"
                }
            }else binding.txtBedsCount.text = "0 / 0"
        }
    }

    suspend fun ArrayList<RoomData.RoomsList>.getCapacity(capacity:(Int)->Unit){
        var count:Int=0
        this.forEach {
            count += it.roomcapacity?.toInt()!!
        }
        capacity.invoke(count)
    }
}


