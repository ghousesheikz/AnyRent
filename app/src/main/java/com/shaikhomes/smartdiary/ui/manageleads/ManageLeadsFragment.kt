package com.shaikhomes.smartdiary.ui.manageleads

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentManageleadsBinding
import com.shaikhomes.smartdiary.databinding.FragmentViewleadsBinding
import com.shaikhomes.smartdiary.ui.home.LeadAdapter
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.WhatsappAccessibilityService
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.isAccessibilityOn
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

class ManageLeadsFragment : Fragment() {
    private var _binding: FragmentManageleadsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var leadType: String? = ""
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    var tempLeadList: List<LeadsList>? = emptyList()
    private var viewLeadsViewModel: ManageLeadsViewModel? = null
    private var leadAdapter: LeadAdapter? = null
    var propertyData: PropertyData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentManageleadsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewLeadsViewModel = ViewModelProvider(this).get(ManageLeadsViewModel::class.java)
        getProperties()
        binding.leadsList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            leadAdapter = LeadAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            leadAdapter?.setLeadClickListener {
                val bundle = Bundle()
                bundle.putString(LEAD_DATA, Gson().toJson(it))
                findNavController().navigate(R.id.action_viewleadFragment_to_leadinfo, bundle)
            }
            leadAdapter?.setAssignToClickListener {
                it.updatedon = currentdate()
                it.update = "update"
                getUsers(it)
            }
            leadAdapter?.setRequirementClickListener {
                val bundle = Bundle()
                bundle.putString(LEAD_DATA, Gson().toJson(it))
                findNavController().navigate(R.id.action_viewleadFragment_to_addrequirement, bundle)
            }
            leadAdapter?.setPriorityClickListener {
                it.updatedon = currentdate()
                //it.createdby = PrefManager(requireContext()).userData?.UserName
                it.update = "update"
                viewLeadsViewModel?.updateLead(leadsList = it, success = {
                    getLeads(leadType)
                }, error = {

                })
            }
            this.adapter = leadAdapter
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    /*if (dy > 0) {
                        binding.layoutHeader.visibility = View.GONE
                    } else {
                        binding.layoutHeader.visibility = View.VISIBLE
                    }*/
                }
            })
        }
        binding.allToggle.isChecked = true
        binding.allToggle.setTextColor(resources.getColor(R.color.c_white_1))
        binding.allToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = ""
                binding.allToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.highToggle.isChecked = false
                binding.mediumToggle.isChecked = false
                binding.lowToggle.isChecked = false
                getLeads(leadType)
            } else {
                binding.allToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.mediumToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "Medium"
                binding.mediumToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.highToggle.isChecked = false
                binding.allToggle.isChecked = false
                binding.lowToggle.isChecked = false
                getLeads(leadType)
            } else {
                binding.mediumToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }

        binding.highToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "High"
                binding.highToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.mediumToggle.isChecked = false
                binding.allToggle.isChecked = false
                binding.lowToggle.isChecked = false
                getLeads(leadType)
            } else {
                binding.highToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.lowToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                leadType = "Low"
                binding.mediumToggle.isChecked = false
                binding.highToggle.isChecked = false
                binding.allToggle.isChecked = false
                binding.lowToggle.setTextColor(resources.getColor(R.color.c_white_1))
                getLeads(leadType)
            } else {
                binding.lowToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filter(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        getLeads(leadType)
        binding.swipeLead.setOnRefreshListener {
            getLeads(leadType)
        }
        return root
    }

    fun filter(text: String) {
        val temp: MutableList<LeadsList?> = ArrayList<LeadsList?>()
        for (d in tempLeadList!!) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.contactnumber?.contains(text) == true) {
                temp.add(d)
            }
        }
        //update recyclerview
        leadAdapter?.updateList(temp as List<LeadsList>,propertyData?.propertyList ?: emptyList())
    }

    private fun getProperties() {
        viewLeadsViewModel?.getProperties("", success = {
            propertyData = it
        }, error = {})
    }

    private fun getLeads(leadType: String?) {
        viewLeadsViewModel?.getLeads(leadType!!, leadType!!, success = {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000)
                if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
                if (it.leadsList.isNotEmpty()) {
                    if (leadType.isEmpty()) {
                        binding.allToggle.setText("All (${it.leadsList.size})")
                    } else if (leadType == "High") {
                        binding.highToggle.setText("High (${it.leadsList.size})")
                    } else if (leadType == "Medium") {
                        binding.mediumToggle.setText("Medium (${it.leadsList.size})")
                    } else if (leadType == "Low") {
                        binding.lowToggle.setText("Low (${it.leadsList.size})")
                    }
                    if (prefmanager.userData?.IsAdmin == "1") {
                        tempLeadList = it.leadsList
                        leadAdapter?.updateList(
                            it.leadsList,
                            propertyData?.propertyList ?: emptyList()
                        )
                    } else {
                        val filter =
                            it.leadsList.filter { it.assignto == prefmanager.userData?.UserName }
                        tempLeadList = filter
                        leadAdapter?.updateList(filter, propertyData?.propertyList ?: emptyList())
                    }
                } else leadAdapter?.updateList(emptyList(), emptyList())
            }
        }, error = {
            if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_logout).setVisible(false)
        menu.findItem(R.id.action_broadcast).setVisible(true)
        menu.findItem(R.id.action_logout).actionView?.visibility = View.GONE
        menu.findItem(R.id.action_delete).actionView?.visibility = View.GONE
        menu.findItem(R.id.action_delete).setVisible(false)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_broadcast) {
            if (!isAccessibilityOn(requireContext(), WhatsappAccessibilityService::class.java)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                requireActivity().startActivity(intent)
            } else {
                showBroadCast()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showBroadCast() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.broadcast_dialog)
        dialog.setCancelable(true)
        val edtMsg = dialog.findViewById<AppCompatEditText>(R.id.edtMsg)
        val btnSend = dialog.findViewById<AppCompatButton>(R.id.btnSend)
        btnSend.setOnClickListener {
            if (edtMsg.text.toString().isNotEmpty()) {
                dialog.dismiss()
                sendBroadcastMessage(edtMsg.text.toString().trim())
            } else showToast(requireContext(), "Please enter message")
        }
        dialog.show()
    }

    private fun sendBroadcastMessage(message: String) {
        leadAdapter?.getList()?.forEach { lead ->
            val i = Intent(Intent.ACTION_VIEW)
            try {
                /*${lead.contactnumber}*/
                val url =
                    "https://api.whatsapp.com/send?phone=${lead.contactnumber}" + "&text=" + URLEncoder.encode(
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


    private fun getUsers(leadData: LeadsList) {
        viewLeadsViewModel?.getUsers(success = {
            it.userDetailsList.let { usersList ->
                val employeeList = usersList.filter { it.IsAdmin == "2" }
                showEmployeeDialog(employeeList) {
                    leadData.assignto = it.UserName
                    viewLeadsViewModel?.updateLead(leadsList = leadData, success = {
                        getLeads(leadType)
                    }, error = {

                    })
                }
            }
        }, error = {

        })
    }

    fun showEmployeeDialog(
        employeeList: List<UserDetailsList>,
        clickListener: (UserDetailsList) -> Unit
    ) {
        val dialog = Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_employee);
        val linearlayout = dialog.findViewById<LinearLayout>(R.id.employeeList)
        linearlayout.removeAllViews()
        employeeList.forEachIndexed { index, userDetailsList ->
            if (!userDetailsList.UserName.isNullOrEmpty()) {
                linearlayout.addView(
                    layoutInflater.inflate(R.layout.item_employee, null)?.apply {
                        this.findViewById<AppCompatTextView>(R.id.textName).apply {
                            text = userDetailsList.UserName
                            setOnClickListener {
                                clickListener.invoke(userDetailsList)
                                dialog.dismiss()
                            }
                        }

                    }
                )
            }
        }
        dialog.setCancelable(true)
        dialog.show();

    }
}