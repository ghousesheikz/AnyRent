package com.shaikhomes.smartdiary.ui.schedulevisit

import android.app.Dialog
import android.content.Intent
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
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentSchedulevisitBinding
import com.shaikhomes.smartdiary.ui.models.LeadscheduleList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.WhatsappAccessibilityService
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.isAccessibilityOn
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScheduleVisitFragment : Fragment() {
    private var _binding: FragmentSchedulevisitBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var scheduleon: String? = currentonlydate()
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    var tempLeadList: List<LeadscheduleList>? = emptyList()
    private var viewLeadsViewModel: ScheduleVisitViewModel? = null
    private var leadAdapter: ScheduleVisitAdapter? = null
    var propertyData: PropertyData? = null
    var assignTo: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSchedulevisitBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewLeadsViewModel = ViewModelProvider(this).get(ScheduleVisitViewModel::class.java)
        getProperties()
        assignTo = if (prefmanager.userData?.IsAdmin == "2") {
            binding.toggleGroup.visibility = View.GONE
            prefmanager.userData?.UserName
        } else ""
        binding.visitList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            leadAdapter = ScheduleVisitAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            leadAdapter?.setLeadClickListener {
                viewLeadsViewModel?.getLeadData(it.contactnumber!!, success = {
                    if (it.leadsList.isNotEmpty()) {
                        val bundle = Bundle()
                        bundle.putString(LEAD_DATA, Gson().toJson(it.leadsList.first()))
                        findNavController().navigate(
                            R.id.action_scheduleFragment_to_leadinfo,
                            bundle
                        )
                    } else {
                        showToast(requireContext(), "No lead registered with this number")
                    }
                }, error = {})

            }
            leadAdapter?.setAssignToClickListener {
                it.updatedon = currentdate()
                it.update = "update"
                getUsers(it)
            }
            this.adapter = leadAdapter
        }
        binding.todayToggle.isChecked = true
        binding.todayToggle.setTextColor(resources.getColor(R.color.c_white_1))
        binding.allToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                scheduleon = ""
                binding.allToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.todayToggle.isChecked = false
                getLeads(assignTo, scheduleon)
            } else {
                binding.allToggle.setTextColor(resources.getColor(R.color.c_black_1))
            }
        }
        binding.todayToggle.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                scheduleon = currentonlydate()
                binding.todayToggle.setTextColor(resources.getColor(R.color.c_white_1))
                binding.allToggle.isChecked = false
                getLeads(assignTo, scheduleon)
            } else {
                binding.todayToggle.setTextColor(resources.getColor(R.color.c_black_1))
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
        getLeads(assignTo, scheduleon)
        binding.swipeLead.setOnRefreshListener {
            getLeads(assignTo, scheduleon)
        }
        return root
    }

    fun filter(text: String) {
        val temp: MutableList<LeadscheduleList?> = ArrayList<LeadscheduleList?>()
        for (d in tempLeadList!!) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.contactnumber?.contains(text) == true) {
                temp.add(d)
            }
        }
        //update recyclerview
        leadAdapter?.updateList(temp as List<LeadscheduleList>)
    }

    private fun getProperties() {
        viewLeadsViewModel?.getProperties("", success = {
            propertyData = it
        }, error = {})
    }

    private fun getLeads(assignto: String?, scheduleon: String?) {
        viewLeadsViewModel?.getLeadSchedule(assignto!!, scheduleon!!, success = {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000)
                if (it.leadscheduleList.isNotEmpty()) {
                    if (scheduleon == "") {
                        binding.allToggle.setText("All (${it.leadscheduleList.size})")
                    } else {
                        binding.todayToggle.setText("Today (${it.leadscheduleList.size})")
                    }
                    tempLeadList = it.leadscheduleList
                    if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
                    leadAdapter?.updateList(it.leadscheduleList)
                }
            }
        }, error = {
            if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_logout).setVisible(false)
        menu.findItem(R.id.action_broadcast).setVisible(false)
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
            } else showToast(requireContext(), "Please enter message")
        }
        dialog.show()
    }


    private fun getUsers(leadData: LeadscheduleList) {
        viewLeadsViewModel?.getUsers(success = {
            it.userDetailsList.let { usersList ->
                val employeeList = usersList.filter { it.IsAdmin == "2" }
                showEmployeeDialog(employeeList) {
                    leadData.assignto = it.UserName
                    viewLeadsViewModel?.updateLead(leadsList = leadData, success = {
                        getLeads(assignTo, scheduleon)
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