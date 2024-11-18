package com.shaikhomes.smartdiary.ui.addrequirement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentAddrequirementBinding
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.utils.ADD_PROPERTY
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PROPERTY_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.showToast


class AddRequirement : Fragment() {

    private var _binding: FragmentAddrequirementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var listMinBudget: ArrayList<String>? = arrayListOf()
    private var listMaxBudget: ArrayList<String>? = arrayListOf()
    var addRequirementViewModel: AddRequirementViewModel? = null
    private var leadsList: LeadsList? = null
    private var propertyData: PropertyList? = null
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var minAmount: String = ""
    private var maxAmount: String = ""
    private var typeOfLead: String = "Rent"
    private var subPropertyType: String = ""
    private var propertyType: String = ""
    private var noOfBedRooms: String = ""
    private var addProperty: Boolean? = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addRequirementViewModel =
            ViewModelProvider(this).get(AddRequirementViewModel::class.java)
        if (!arguments?.getString(LEAD_DATA).isNullOrEmpty()) {
            leadsList = Gson().fromJson(arguments?.getString(LEAD_DATA), LeadsList::class.java)
        }
        addProperty = arguments?.getBoolean(ADD_PROPERTY, false)
        if (!arguments?.getString(PROPERTY_DATA).isNullOrEmpty()) {
            propertyData =
                Gson().fromJson(arguments?.getString(PROPERTY_DATA), PropertyList::class.java)
        }
        _binding = FragmentAddrequirementBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (leadsList?.typeoflead?.toLowerCase() == "buy") {
            buyFunctionality()
        } else if (leadsList?.typeoflead?.toLowerCase() == "rent") {
            rentFunctionality()
        } else rentFunctionality()
        if (!leadsList?.typeoflead.isNullOrEmpty()) {
            binding.rentToggle.isEnabled = false
            binding.buyToggle.isEnabled = false
            binding.pgToggle.isEnabled = false
        }
        handleToggles()
        binding.btnSave.setOnClickListener(clickListener)
        return root
    }

    private val clickListener = SafeClickListener {
        when (it?.id) {
            R.id.btnSave -> {
                if (binding.btnSave.text.toString().toLowerCase() == "update") {
                    updateData()
                } else saveData()
            }
        }
    }

    private fun updateData() {
        val propertyList = PropertyList(
            ID = propertyData?.ID,
            contactnumber = propertyData?.contactnumber,
            subpropertytype = propertyData?.subpropertytype,
            typeoflead = propertyData?.typeoflead,
            propertytype = propertyData?.propertytype,
            leadsname = propertyData?.leadsname,
            noofbedrooms = propertyData?.noofbedrooms,
            leadrole = "",
            project = binding.edtSpecifyProject.text.toString().trim(),
            priority = propertyData?.priority,
            locations = binding.edtSpecifyLocality.text.toString(),
            leadid = propertyData?.leadid,
            assignto = propertyData?.assignto,
            leadstatus = propertyData?.leadstatus,
            createdby = propertyData?.createdby,
            updatedon = currentdate(),
            maxamount = maxAmount,
            minamount = minAmount,
            update = "update"
        )
        addRequirementViewModel?.addProperty(propertyList, success = {
            showToast(requireContext(), "Property Updated Successfully")
            requireActivity().onBackPressed()
        }, error = {
            showToast(requireContext(), it)
        })
    }

    private fun saveData() {
        val propertyList = PropertyList(
            contactnumber = if(addProperty==true) "" else leadsList?.contactnumber,
            subpropertytype = subPropertyType,
            typeoflead = typeOfLead,
            propertytype = propertyType,
            leadsname =if(addProperty==true) "" else leadsList?.leadsname,
            noofbedrooms = noOfBedRooms,
            leadrole = "",
            project = binding.edtSpecifyProject.text.toString().trim(),
            priority = if(addProperty==true) "" else leadsList?.priority,
            locations = binding.edtSpecifyLocality.text.toString(),
            leadid = if(addProperty==true) 0 else leadsList?.ID,
            assignto = if(addProperty==true) "" else leadsList?.assignto,
            leadstatus = if(addProperty==true) "" else leadsList?.leadstatus,
            createdby = prefmanager.userData?.UserName,
            updatedon = currentdate(),
            maxamount = maxAmount,
            minamount = minAmount
        )
        addRequirementViewModel?.addProperty(propertyList, success = {
            showToast(requireContext(), "Property Saved Successfully")
            requireActivity().onBackPressed()
        }, error = {
            showToast(requireContext(), it)
        })
    }

    private fun handleToggles() {
        propertyData.let {
            binding.edtSpecifyLocality.setText(propertyData?.locations ?: "")
            binding.edtSpecifyProject.setText(propertyData?.project ?: "")
            if (propertyData?.propertytype?.toLowerCase() == "residential") {
                binding.residentialToggle.isChecked = true
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.residentialToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.residentialGroup.visibility = View.VISIBLE
                binding.commercialGroup.visibility = View.GONE
            } else if (propertyData?.propertytype?.toLowerCase() == "commercial") {
                binding.commercialToggle.isChecked = true
                binding.bedroomsGroup.visibility = View.GONE
                binding.commercialToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.residentialGroup.visibility = View.GONE
                binding.commercialGroup.visibility = View.VISIBLE
            }
            if (!propertyData?.noofbedrooms.isNullOrEmpty()) {
                checkBedrooms(propertyData?.noofbedrooms)
            }
            if (!propertyData?.subpropertytype.isNullOrEmpty()) {
                checkSubProperty(propertyData?.subpropertytype)
            }
            if (!propertyData?.propertytype.isNullOrEmpty()) {
                binding.btnSave.text = "UPDATE"
            }
        }
        binding.buyToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                typeOfLead = "Buy"
                propertyData?.typeoflead = "Buy"
                binding.buyToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.pgToggle.isChecked = false
                binding.rentToggle.isChecked = false
                buyFunctionality()
            } else {
                binding.buyToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.rentToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                typeOfLead = "Rent"
                propertyData?.typeoflead = "Rent"
                binding.rentToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.pgToggle.isChecked = false
                binding.buyToggle.isChecked = false
                rentFunctionality()
            } else {
                binding.rentToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.pgToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                typeOfLead = "Pg"
                propertyData?.typeoflead = "Pg"
                binding.buyToggle.isChecked = false
                binding.rentToggle.isChecked = false
                binding.pgToggle.setTextColor(resources.getColor(R.color.c_white_1))
                pgFunctionality()
            } else {
                binding.pgToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.residentialToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                propertyType = "Residential"
                propertyData?.propertytype = "Residential"
                binding.commercialToggle.isChecked = false
                binding.residentialToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.residentialGroup.visibility = View.VISIBLE
                binding.commercialGroup.visibility = View.GONE
            } else {
                binding.residentialToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.commercialToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                propertyType = "Commercial"
                propertyData?.propertytype = "Commercial"
                binding.residentialToggle.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.commercialToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.residentialGroup.visibility = View.GONE
                binding.commercialGroup.visibility = View.VISIBLE
                binding.bedroomsGroup.visibility = View.GONE
                clearBedrooms()
            } else {
                binding.commercialToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.SubProToggle1.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Flat"
                propertyData?.subpropertytype = "Flat"
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.SubProToggle1.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle1.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle2.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Builder Floor"
                propertyData?.subpropertytype = "Builder Floor"
                binding.SubProToggle1.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.SubProToggle2.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle2.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle3.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "House"
                propertyData?.subpropertytype = "House"
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle1.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.SubProToggle3.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle3.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle4.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Plot"
                propertyData?.subpropertytype = "Plot"
                noOfBedRooms = ""
                clearBedrooms()
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle1.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle4.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle4.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle5.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Villa"
                propertyData?.subpropertytype = "Villa"
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle1.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.SubProToggle5.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle5.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle6.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Studio Apartment"
                propertyData?.subpropertytype = "Studio Apartment"
                noOfBedRooms = ""
                clearBedrooms()
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle1.isChecked = false
                binding.SubProToggle7.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle6.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle6.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle7.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "PentHouse"
                propertyData?.subpropertytype = "PentHouse"
                binding.SubProToggle2.isChecked = false
                binding.SubProToggle3.isChecked = false
                binding.SubProToggle4.isChecked = false
                binding.SubProToggle5.isChecked = false
                binding.SubProToggle6.isChecked = false
                binding.SubProToggle1.isChecked = false
                binding.bedroomsGroup.visibility = View.VISIBLE
                binding.SubProToggle7.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle7.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.SubProToggle8.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Office Space"
                propertyData?.subpropertytype = "Office Space"
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.SubProToggle8.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle8.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle9.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Shop"
                propertyData?.subpropertytype = "Shop"
                binding.SubProToggle8.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle9.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle9.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle10.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Showroom"
                propertyData?.subpropertytype = "Showroom"
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle8.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle10.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle10.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle11.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Warehouse"
                propertyData?.subpropertytype = "Warehouse"
                noOfBedRooms = ""
                clearBedrooms()
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle8.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle11.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle11.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle12.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Commercial Land"
                propertyData?.subpropertytype = "Commercial Land"
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle8.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle12.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle12.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle13.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Space in mall"
                propertyData?.subpropertytype = "Space in mall"
                noOfBedRooms = ""
                clearBedrooms()
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle8.isChecked = false
                binding.SubProToggle14.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle13.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle13.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.SubProToggle14.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subPropertyType = "Office in ITPark/SEZ"
                propertyData?.subpropertytype = "Office in ITPark/SEZ"
                binding.SubProToggle9.isChecked = false
                binding.SubProToggle10.isChecked = false
                binding.SubProToggle11.isChecked = false
                binding.SubProToggle12.isChecked = false
                binding.SubProToggle13.isChecked = false
                binding.SubProToggle8.isChecked = false
                binding.bedroomsGroup.visibility = View.GONE
                binding.SubProToggle14.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.SubProToggle14.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.BedToggle1.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                noOfBedRooms = "1BHK"
                propertyData?.noofbedrooms = "1BHK"
                binding.BedToggle2.isChecked = false
                binding.BedToggle3.isChecked = false
                binding.BedToggle4.isChecked = false
                binding.BedToggle5.isChecked = false
                binding.BedToggle1.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.BedToggle1.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.BedToggle2.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                noOfBedRooms = "2BHK"
                propertyData?.noofbedrooms = "2BHK"
                binding.BedToggle1.isChecked = false
                binding.BedToggle3.isChecked = false
                binding.BedToggle4.isChecked = false
                binding.BedToggle5.isChecked = false
                binding.BedToggle2.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.BedToggle2.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.BedToggle3.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                noOfBedRooms = "3BHK"
                propertyData?.noofbedrooms = "3BHK"
                binding.BedToggle1.isChecked = false
                binding.BedToggle2.isChecked = false
                binding.BedToggle4.isChecked = false
                binding.BedToggle5.isChecked = false
                binding.BedToggle3.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.BedToggle3.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.BedToggle4.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                noOfBedRooms = "4BHK"
                propertyData?.noofbedrooms = "4BHK"
                binding.BedToggle2.isChecked = false
                binding.BedToggle3.isChecked = false
                binding.BedToggle1.isChecked = false
                binding.BedToggle5.isChecked = false
                binding.BedToggle4.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.BedToggle4.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.BedToggle5.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                noOfBedRooms = ">4BHK"
                propertyData?.noofbedrooms = ">4BHK"
                binding.BedToggle2.isChecked = false
                binding.BedToggle3.isChecked = false
                binding.BedToggle4.isChecked = false
                binding.BedToggle1.isChecked = false
                binding.BedToggle5.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.BedToggle5.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        listMinBudget?.forEachIndexed { index, min ->
            if (min == propertyData?.minamount) {
                binding.minSpinner.setSelection(index)
            }
        }
        listMaxBudget?.forEachIndexed { index, min ->
            if (min == propertyData?.maxamount) {
                binding.maxSpinner.setSelection(index)
            }
        }
        binding.minSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                minAmount = if (selectedItem != "Min") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.maxSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                maxAmount = if (selectedItem != "Max") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun checkSubProperty(subpropertytype: String?) {
        when (subpropertytype?.toLowerCase()) {
            "flat" -> {
                binding.SubProToggle1.isChecked = true
                binding.SubProToggle1.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "builder floor" -> {
                binding.SubProToggle2.isChecked = true
                binding.SubProToggle2.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "house" -> {
                binding.SubProToggle3.isChecked = true
                binding.SubProToggle3.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "plot" -> {
                binding.SubProToggle4.isChecked = true
                binding.SubProToggle4.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "villa" -> {
                binding.SubProToggle5.isChecked = true
                binding.SubProToggle5.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "studio apartment" -> {
                binding.SubProToggle6.isChecked = true
                binding.SubProToggle6.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "penthouse" -> {
                binding.SubProToggle7.isChecked = true
                binding.SubProToggle7.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "officespace" -> {
                binding.SubProToggle8.isChecked = true
                binding.SubProToggle8.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "shop" -> {
                binding.SubProToggle9.isChecked = true
                binding.SubProToggle9.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "showroom" -> {
                binding.SubProToggle10.isChecked = true
                binding.SubProToggle10.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "warehouse" -> {
                binding.SubProToggle11.isChecked = true
                binding.SubProToggle11.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "commercial land" -> {
                binding.SubProToggle12.isChecked = true
                binding.SubProToggle12.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "space in mall" -> {
                binding.SubProToggle13.isChecked = true
                binding.SubProToggle13.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "office in itpark/sez" -> {
                binding.SubProToggle14.isChecked = true
                binding.SubProToggle14.setTextColor(resources.getColor(R.color.c_white_1))
            }
        }
    }

    private fun checkBedrooms(noofbedrooms: String?) {
        when (noofbedrooms) {
            "1BHK" -> {
                binding.BedToggle1.isChecked = true
                binding.BedToggle1.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "2BHK" -> {
                binding.BedToggle2.isChecked = true
                binding.BedToggle2.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "3BHK" -> {
                binding.BedToggle3.isChecked = true
                binding.BedToggle3.setTextColor(resources.getColor(R.color.c_white_1))
            }

            "4BHK" -> {
                binding.BedToggle4.isChecked = true
                binding.BedToggle4.setTextColor(resources.getColor(R.color.c_white_1))
            }

            ">4BHK" -> {
                binding.BedToggle5.isChecked = true
                binding.BedToggle5.setTextColor(resources.getColor(R.color.c_white_1))
            }
        }
    }

    fun clearBedrooms() {
        noOfBedRooms = ""
        binding.BedToggle1.isChecked = false
        binding.BedToggle2.isChecked = false
        binding.BedToggle3.isChecked = false
        binding.BedToggle4.isChecked = false
        binding.BedToggle5.isChecked = false
    }

    private fun buyFunctionality() {
        typeOfLead = "Buy"
        binding.buyToggle.isChecked = true
        binding.buyToggle.setTextColor(resources.getColor(R.color.c_white_1))
        if (listMinBudget?.size!! > 0) listMinBudget?.clear()
        if (listMaxBudget?.size!! > 0) listMaxBudget?.clear()
        listMinBudget.addMinBudget()
        listMaxBudget.addMaxBudget()
        binding.minSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMinBudget!!
        )
        binding.maxSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMaxBudget!!
        )
    }

    private fun rentFunctionality() {
        typeOfLead = "Rent"
        binding.rentToggle.isChecked = true
        binding.rentToggle.setTextColor(resources.getColor(R.color.c_white_1))
        if (listMinBudget?.size!! > 0) listMinBudget?.clear()
        if (listMaxBudget?.size!! > 0) listMaxBudget?.clear()
        listMinBudget.addMinRentBudget()
        listMaxBudget.addMaxRentBudget()
        binding.minSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMinBudget!!
        )
        binding.maxSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMaxBudget!!
        )
    }

    private fun pgFunctionality() {
        typeOfLead = "Pg"
        binding.pgToggle.isChecked = true
        binding.pgToggle.setTextColor(resources.getColor(R.color.c_white_1))
        if (listMinBudget?.size!! > 0) listMinBudget?.clear()
        if (listMaxBudget?.size!! > 0) listMaxBudget?.clear()
        listMinBudget.addMinRentBudget()
        listMaxBudget.addMaxRentBudget()
        binding.minSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMinBudget!!
        )
        binding.maxSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item, listMaxBudget!!
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun java.util.ArrayList<String>?.addMinBudget() {
    this?.add("Min")
    this?.add("5 Lac")
    this?.add("10 Lac")
    this?.add("20 Lac")
    this?.add("30 Lac")
    this?.add("40 Lac")
    this?.add("50 Lac")
    this?.add("60 Lac")
    this?.add("70 Lac")
    this?.add("80 Lac")
    this?.add("90 Lac")
    this?.add("1 Cr")
    this?.add("1.2 Cr")
    this?.add("1.4 Cr")
    this?.add("1.6 Cr")
    this?.add("1.8 Cr")
    this?.add("2 Cr")
}

private fun java.util.ArrayList<String>?.addMaxBudget() {
    this?.add("Max")
    this?.add("5 Lac")
    this?.add("10 Lac")
    this?.add("20 Lac")
    this?.add("30 Lac")
    this?.add("40 Lac")
    this?.add("50 Lac")
    this?.add("60 Lac")
    this?.add("70 Lac")
    this?.add("80 Lac")
    this?.add("90 Lac")
    this?.add("1 Cr")
    this?.add("1.2 Cr")
    this?.add("1.4 Cr")
    this?.add("1.6 Cr")
    this?.add("1.8 Cr")
    this?.add("2 Cr")
}

private fun java.util.ArrayList<String>?.addMinRentBudget() {
    this?.add("Min")
    this?.add("1000")
    this?.add("5000")
    this?.add("10000")
    this?.add("15000")
    this?.add("20000")
    this?.add("25000")
    this?.add("30000")
    this?.add("35000")
    this?.add("40000")
    this?.add("50000")
    this?.add("60000")
    this?.add("70000")
    this?.add("85000")
    this?.add("1 lac")
    this?.add("1.5 lacs")
    this?.add("2 lacs")
    this?.add("4 lacs")
    this?.add("7 lacs")
    this?.add("10 lacs")
    this?.add(">10 lacs")
}

private fun java.util.ArrayList<String>?.addMaxRentBudget() {
    this?.add("Max")
    this?.add("1000")
    this?.add("5000")
    this?.add("10000")
    this?.add("15000")
    this?.add("20000")
    this?.add("25000")
    this?.add("30000")
    this?.add("35000")
    this?.add("40000")
    this?.add("50000")
    this?.add("60000")
    this?.add("70000")
    this?.add("85000")
    this?.add("1 lac")
    this?.add("1.5 lacs")
    this?.add("2 lacs")
    this?.add("4 lacs")
    this?.add("7 lacs")
    this?.add("10 lacs")
    this?.add(">10 lacs")
}
