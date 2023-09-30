package com.shaikhomes.smartdiary.ui.addlead

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentAddleadBinding
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.hideKeyboard


class AddLeadFragment : Fragment() {

    private var _binding: FragmentAddleadBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var leadType: String? = "Rent"
    private var leadRole: String? = ""
    private var lookingFor: String? = ""
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var leadsList: LeadsList? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddleadBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val addLeadViewModel =
            ViewModelProvider(this).get(AddLeadViewModel::class.java)
        if (!arguments?.getString(LEAD_DATA).isNullOrEmpty()) {
            leadsList = Gson().fromJson(arguments?.getString(LEAD_DATA), LeadsList::class.java)
        }
        if (leadsList?.typeoflead.isNullOrEmpty()) {
            binding.rentToggle.isChecked = true
            binding.rentToggle.setTextColor(resources.getColor(R.color.c_white_1))
        }
        leadsList.let { leadData ->
            if (!leadData?.leadsname.isNullOrEmpty()) {
                binding.edtContactNumber.setText(leadData?.contactnumber)
                binding.edtLeadsName.setText(leadData?.leadsname)
                lookingFor = leadData?.lookingfor
                if (leadData?.typeoflead?.toLowerCase() == "buy") {
                    binding.buyToggle.isChecked = true
                    binding.buyToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.typeoflead?.toLowerCase() == "rent") {
                    binding.rentToggle.isChecked = true
                    binding.rentToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.typeoflead?.toLowerCase() == "sell") {
                    binding.sellToggle.isChecked = true
                    binding.sellToggle.setTextColor(resources.getColor(R.color.c_white_1))
                    binding.leadRoleGroup.isVisible = true
                    if (leadData.leadrole?.toLowerCase() == "owner") {
                        binding.ownerToggle.isChecked = true
                        binding.ownerToggle.setTextColor(resources.getColor(R.color.c_white_1))
                    } else if (leadData.leadrole?.toLowerCase() == "builder") {
                        binding.builderToggle.isChecked = true
                        binding.builderToggle.setTextColor(resources.getColor(R.color.c_white_1))
                    } else if (leadData.leadrole?.toLowerCase() == "agent") {
                        binding.agentToggle.isChecked = true
                        binding.agentToggle.setTextColor(resources.getColor(R.color.c_white_1))
                    }
                }
                if (leadData?.priority?.toLowerCase() == "high") {
                    binding.highToggle.isChecked = true
                    binding.highToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.priority?.toLowerCase() == "medium") {
                    binding.mediumToggle.isChecked = true
                    binding.mediumToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.priority?.toLowerCase() == "low") {
                    binding.lowToggle.isChecked = true
                    binding.lowToggle.setTextColor(resources.getColor(R.color.c_white_1))
                }
                if (leadData?.lookingfor?.toLowerCase() == "male") {
                    binding.maleToggle.isChecked = true
                    binding.maleToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.lookingfor?.toLowerCase() == "female") {
                    binding.femaleToggle.isChecked = true
                    binding.femaleToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.lookingfor?.toLowerCase() == "family") {
                    binding.familyToggle.isChecked = true
                    binding.familyToggle.setTextColor(resources.getColor(R.color.c_white_1))
                } else if (leadData?.lookingfor?.toLowerCase() == "couples") {
                    binding.couplesToggle.isChecked = true
                    binding.couplesToggle.setTextColor(resources.getColor(R.color.c_white_1))
                }
                handleToggles()
                binding.priorityGroup.visibility = View.VISIBLE
                binding.btnNext.text = "update"
            }
        }
        binding.buyToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "Buy"
                leadsList?.leadrole = ""
                leadRole = ""
                leadsList?.typeoflead = "Buy"
                binding.buyToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.sellToggle.isChecked = false
                binding.rentToggle.isChecked = false
                binding.leadRoleGroup.isVisible = false
                leadRole = ""
            } else {
                binding.buyToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.rentToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "Rent"
                leadsList?.typeoflead = "Rent"
                leadsList?.leadrole = ""
                leadRole = ""
                binding.rentToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.sellToggle.isChecked = false
                binding.buyToggle.isChecked = false
                binding.leadRoleGroup.isVisible = false
                leadRole = ""
            } else {
                binding.rentToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.sellToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "Sell"
                leadsList?.typeoflead = "Sell"
                binding.buyToggle.isChecked = false
                binding.rentToggle.isChecked = false
                binding.sellToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.leadRoleGroup.isVisible = true
                leadRole = "Owner"
            } else {
                binding.sellToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.ownerToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadRole = "Owner"
                leadsList?.leadrole = "Owner"
                binding.builderToggle.isChecked = false
                binding.agentToggle.isChecked = false
                binding.ownerToggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.ownerToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.builderToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadRole = "Builder"
                leadsList?.leadrole = "Builder"
                binding.ownerToggle.isChecked = false
                binding.agentToggle.isChecked = false
                binding.builderToggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.builderToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.agentToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadRole = "Agent"
                leadsList?.leadrole = "Agent"
                binding.ownerToggle.isChecked = false
                binding.builderToggle.isChecked = false
                binding.agentToggle.setTextColor(resources.getColor(R.color.c_white_1))
            } else {
                binding.agentToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.maleToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                lookingFor = "Male"
                leadsList?.lookingfor = "Male"
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
                lookingFor = "Female"
                leadsList?.lookingfor = "Female"
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
                lookingFor = "Family"
                leadsList?.lookingfor = "Family"
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
                lookingFor = "Couples"
                leadsList?.lookingfor = "Couples"
                binding.couplesToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.maleToggle.isChecked = false
                binding.femaleToggle.isChecked = false
                binding.familyToggle.isChecked = false
            } else {
                binding.couplesToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.btnNext.setOnClickListener { view ->
            if (validations()) {
                requireActivity().hideKeyboard(view)
                val leadsData = if (!leadsList?.leadsname.isNullOrEmpty()) {
                    leadsList?.update = "update"
                    leadsList?.date = currentdate()
                    leadsList?.updatedon = currentdate()
                    leadsList?.contactnumber = binding.edtContactNumber.text.toString().trim()
                    leadsList?.leadsname = binding.edtLeadsName.text.toString().trim()
                    leadsList?.countrycode = binding.prefixNumber.text.toString()
                    leadsList
                } else {
                    LeadsList(
                        contactnumber = binding.edtContactNumber.text.toString().trim(),
                        date = currentdate(),
                        email = binding.edtEmail.text.toString().trim(),
                        leadsname = binding.edtLeadsName.text.toString().trim(),
                        locations = "",
                        maxamount = "0",
                        minamount = "0",
                        priority = "",
                        project = "",
                        registerno = "",
                        typeoflead = leadType,
                        calldetails = "",
                        leadstatus = "Created",
                        leadrole = leadRole,
                        createdby = prefmanager.userData?.UserName.toString(),
                        assignto = if (prefmanager.userData?.IsAdmin == "1") "" else prefmanager.userData?.UserName.toString(),
                        updatedon = currentdate(),
                        countrycode = binding.prefixNumber.text.toString(),
                        lookingfor = lookingFor,
                        update = null
                    )
                }
                addLeadViewModel.addLead(leadsData!!, success = {
                    emptyData()
                    Toast.makeText(requireContext(), "Lead Added Successfully", Toast.LENGTH_SHORT)
                        .show()
                    addLeadViewModel.getLeads(
                        leadsData.contactnumber!!, success = {
                            if (it.leadsList.isNotEmpty()) {
                                val bundle = Bundle()
                                bundle.putString(LEAD_DATA, Gson().toJson(it.leadsList.first()))
                                findNavController().navigate(
                                    R.id.action_addleadFragment_to_addrequirement,
                                    bundle
                                )
                            }
                        }, error = {}
                    )
                }, error = {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                })

            }
        }
        binding.prefixNumber.setOnClickListener {
            showCountryCodeDialog { countryCode ->
                if (countryCode == "+91") {
                    binding.prefixNumber.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_india,
                        0,
                        0,
                        0
                    )
                } else if (countryCode == "+971") {
                    binding.prefixNumber.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_uae,
                        0,
                        0,
                        0
                    )
                }
                binding.prefixNumber.setText(countryCode)
            }
        }
        return root
    }

    fun showCountryCodeDialog(
        clickListener: (String) -> Unit
    ) {
        val countryCodeList = arrayListOf("+91", "+971")
        val dialog = Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_employee);
        dialog.findViewById<AppCompatTextView>(R.id.titleHeader).apply {
            text = "Country Code"
        }
        val linearlayout = dialog.findViewById<LinearLayout>(R.id.employeeList)
        linearlayout.removeAllViews()
        countryCodeList.forEachIndexed { index, userDetailsList ->
            linearlayout.addView(
                layoutInflater.inflate(R.layout.item_employee, null)?.apply {
                    this.findViewById<AppCompatTextView>(R.id.textName).apply {
                        if (userDetailsList == "+91") {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_india, 0, 0, 0
                            )
                        } else if (userDetailsList == "+971") {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_uae, 0, 0, 0
                            )
                        }
                        text = userDetailsList
                        setOnClickListener {
                            clickListener.invoke(userDetailsList)
                            dialog.dismiss()
                        }
                    }

                }
            )
        }
        dialog.setCancelable(true)
        dialog.show();

    }

    private fun handleToggles() {
        binding.highToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList?.priority = "High"
                binding.highToggle.setTextColor(requireContext().resources.getColor(R.color.c_white_1))
                binding.lowToggle.isChecked = false
                binding.mediumToggle.isChecked = false
            } else {
                binding.highToggle.setTextColor(requireContext().resources.getColor(R.color.c_black_1))
            }
        }

        binding.mediumToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList?.priority = "Medium"
                binding.mediumToggle.setTextColor(requireContext().resources.getColor(R.color.c_white_1))
                binding.lowToggle.isChecked = false
                binding.highToggle.isChecked = false
            } else {
                binding.mediumToggle.setTextColor(requireContext().resources.getColor(R.color.c_black_1))
            }
        }
        binding.lowToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadsList?.priority = "Low"
                binding.highToggle.isChecked = false
                binding.mediumToggle.isChecked = false
                binding.lowToggle.setTextColor(requireContext().resources.getColor(R.color.c_white_1))
            } else {
                binding.lowToggle.setTextColor(requireContext().resources.getColor(R.color.c_black_1))
            }
        }
    }

    private fun emptyData() {
        binding.edtContactNumber.setText("")
        binding.edtEmail.setText("")
        binding.edtLeadsName.setText("")
        binding.rentToggle.isChecked = true
        binding.priorityGroup.visibility = View.GONE
        leadType = "Rent"
        leadRole = ""
    }

    private fun validations(): Boolean {
        var flag: Boolean = true
        if (binding.edtLeadsName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Enter Leads Name", Toast.LENGTH_SHORT).show()
        } else if (binding.edtContactNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Enter Leads Contact number", Toast.LENGTH_SHORT)
                .show()
        } else if (binding.edtEmail.text.toString().isNotEmpty()) {
            if (!isValidEmail(binding.edtEmail.text.toString().trim())) flag = false
            Toast.makeText(requireContext(), "Enter valid email", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}