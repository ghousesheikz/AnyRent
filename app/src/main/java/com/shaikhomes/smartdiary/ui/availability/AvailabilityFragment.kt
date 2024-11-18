package com.shaikhomes.smartdiary.ui.availability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentAvailabilityBinding
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.AvailabilityList
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.utils.AVAILABILITIES_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.showToast

class AvailabilityFragment : Fragment() {
    private var _binding: FragmentAvailabilityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    var tempLeadList: List<LeadsList>? = emptyList()
    private var availabilityViewModel: AvailablityViewModel? = null
    private var availabilityAdapter: AvailabilityAdapter? = null
    var propertyData: PropertyData? = null
    var apartmentfor: String = ""
    var room1: String = "Not Available"
    var room2: String = "Not Available"
    var room3: String = "Not Available"
    var room4: String = "Not Available"
    private var availabilityList: AvailabilityList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAvailabilityBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (!arguments?.getString(AVAILABILITIES_DATA).isNullOrEmpty()) {
            availabilityList = Gson().fromJson(
                arguments?.getString(AVAILABILITIES_DATA),
                AvailabilityList::class.java
            )
        }
        availabilityList?.apartmentname.let {
            binding.txtApartmentName.text = it
        }
        availabilityViewModel = ViewModelProvider(this).get(AvailablityViewModel::class.java)
        binding.maleToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                apartmentfor = "Male"
                binding.maleToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.femaleToggle.isChecked = false
                binding.familyToggle.isChecked = false
                binding.couplesToggle.isChecked = false
            } else {
                binding.maleToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.femaleToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                apartmentfor = "Female"
                binding.femaleToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.maleToggle.isChecked = false
                binding.familyToggle.isChecked = false
                binding.couplesToggle.isChecked = false
            } else {
                binding.femaleToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.familyToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                apartmentfor = "Family"
                binding.familyToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.maleToggle.isChecked = false
                binding.femaleToggle.isChecked = false
                binding.couplesToggle.isChecked = false
            } else {
                binding.familyToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.couplesToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                apartmentfor = "Couples"
                binding.couplesToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.maleToggle.isChecked = false
                binding.femaleToggle.isChecked = false
                binding.familyToggle.isChecked = false
            } else {
                binding.couplesToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.room1Toggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                room1 = "Available"
                binding.room1Toggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                room1 = "Not Available"
                binding.room1Toggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.room2Toggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                room2 = "Available"
                binding.room2Toggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                room2 = "Not Available"
                binding.room2Toggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.room3Toggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                room3 = "Available"
                binding.room3Toggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                room3 = "Not Available"
                binding.room3Toggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.room4Toggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                room4 = "Available"
                binding.room4Toggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                room4 = "Not Available"
                binding.room4Toggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.btnSave.setOnClickListener(clickListener)
        binding.availabilityList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            availabilityAdapter = AvailabilityAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            this.adapter = availabilityAdapter
        }
        getAvailabilities()
        return root
    }

    private fun getAvailabilities() {
        availabilityViewModel?.getAvailabilities(availabilityList?.apartmentid!!, success = {
            if (it.availabilityList.isNotEmpty()) {
                availabilityAdapter?.updateList(it.availabilityList)
            }
        }, error = {
            showToast(requireContext(), it)
        })
    }

    private val clickListener = SafeClickListener {
        when (it?.id) {
            R.id.btnSave -> {
                val availabilityList = AvailabilityList(
                    apartmentid = availabilityList?.apartmentid,
                    apartmentname = availabilityList?.apartmentname,
                    apartmentfor = apartmentfor,
                    flat = binding.edtFlat.text.toString(),
                    room1 = room1,
                    room2 = room2,
                    room3 = room3,
                    room4 = room4,
                    createdby = prefmanager.userData?.UserName,
                    updatedon = currentdate()
                )
                availabilityViewModel?.postAvailability(availabilityList, success = {
                    showToast(requireContext(), "Availability Added")
                    emptyData()
                    getAvailabilities()
                }, error = {
                    showToast(requireContext(), it)
                })
            }
        }
    }

    private fun emptyData() {
        binding.edtFlat.setText("")
        binding.maleToggle.isChecked = false
        binding.femaleToggle.isChecked = false
        binding.familyToggle.isChecked = false
        binding.couplesToggle.isChecked = false
        binding.room1Toggle.isChecked = false
        binding.room2Toggle.isChecked = false
        binding.room3Toggle.isChecked = false
        binding.room4Toggle.isChecked = false
        apartmentfor = ""
        room1 = "Not Available"
        room2 = "Not Available"
        room3 = "Not Available"
        room4 = "Not Available"
    }

}