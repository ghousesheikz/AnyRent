package com.shaikhomes.anyrent.ui.apartment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentAddApartmentBinding
import com.shaikhomes.anyrent.ui.customviews.SafeClickListener
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.hideKeyboard
import com.shaikhomes.anyrent.ui.utils.randomNumber
import com.shaikhomes.anyrent.ui.utils.showToast


class AddApartmentFragment : Fragment() {

    private var _binding: FragmentAddApartmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddApartmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        binding.btnSave.setOnClickListener(clickListener)
        getApartments()
        return root
    }

    private val clickListener = SafeClickListener {
        when (it?.id) {
            R.id.btnSave -> {
                if (validations()) {
                    requireActivity().hideKeyboard(it)
                    val apartmentData = ApartmentList(
                        userid = randomNumber().toString(),
                        apartmentname = binding.edtApartmentName.text.toString(),
                        apartmentfor = "",
                        createdby = prefmanager.userData?.UserName,
                        updatedon = currentdate()
                    )
                    addApartmentViewModel?.addApartment(apartmentData, success = {
                        if (it.status == "200") {
                            emptyData()
                            getApartments()
                        }
                    }, error = {
                        showToast(requireContext(), it)
                    })
                }
            }
        }
    }

    fun getApartments() {
//        addApartmentViewModel?.getApartments(success = {
//            if (it.apartmentList.isNotEmpty()) {
//                binding.apartmentList.removeAllViews()
//                it.apartmentList.forEachIndexed { index, apartmentList ->
//                    binding.apartmentList.addView(
//                        layoutInflater.inflate(R.layout.item_apartment, null)?.apply {
//                            this.findViewById<AppCompatTextView>(R.id.textApartmentName).apply {
//                                text = apartmentList.apartmentname
//                                setOnClickListener {
//                                    val bundle = Bundle()
//                                    bundle.putString(AVAILABILITIES_DATA, Gson().toJson(apartmentList))
//                                    findNavController().navigate(R.id.action_availabilityFragment_to_availability, bundle)
//                                }
//                            }
//
//                        }
//                    )
//                }
//            }
//        }, error = {
//            showToast(requireContext(), it)
//        })
    }


    private fun emptyData() {
        binding.edtApartmentName.setText("")
    }

    private fun validations(): Boolean {
        var flag: Boolean = true
        if (binding.edtApartmentName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Enter Apartment Name", Toast.LENGTH_SHORT).show()
        }
        return flag
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}