package com.shaikhomes.smartdiary.ui.home

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.gson.Gson
import com.shaikhomes.smartdiary.MainActivity
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentHomeBinding
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.LeadscheduleList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.ADD_PROPERTY
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.currentonlydate
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var leadAdapter: LeadAdapter? = null
    var homeViewModel: HomeViewModel? = null
    var assignTo: String = ""
    var propertyData: PropertyData? = null
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.leadsNav.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addleadFragment)
        }
        binding.availabilityNav.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addapartment)
        }
        binding.propertyNav.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(ADD_PROPERTY, true)
            findNavController().navigate(R.id.action_homeFragment_to_addproperty)
        }
        PrefManager(requireContext()).userData.let {
            assignTo = if (it?.IsAdmin == "1") "" else it?.UserName.toString()
            binding.txtUserName.text = it?.UserName
            binding.txtLocation.text = it?.Address
        }
        binding.layoutProperties.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_propertylist)
        }
        binding.leadsList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            leadAdapter = LeadAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            leadAdapter?.setLeadClickListener { it, pos ->
                prefmanager.listPos = pos
                val bundle = Bundle()
                bundle.putString(LEAD_DATA, Gson().toJson(it))
                findNavController().navigate(R.id.action_homeFragment_to_leadinfo, bundle)
            }
            leadAdapter?.setStatusClickListener {
                it.update = "status"
                setStatus(it)
            }
            leadAdapter?.setAssignToClickListener {
                it.updatedon = currentdate()
                it.update = "update"
                getUsers(it)
            }
            leadAdapter?.setRequirementClickListener {
                val bundle = Bundle()
                bundle.putString(LEAD_DATA, Gson().toJson(it))
                findNavController().navigate(R.id.action_homeFragment_to_addrequirement, bundle)
            }
            leadAdapter?.setPriorityClickListener {
                it.updatedon = currentdate()
                //it.createdby = PrefManager(requireContext()).userData?.UserName
                it.update = "update"
                homeViewModel?.updateLead(leadsList = it, success = {
                    getLeads(assignTo)
                }, error = {

                })
            }
            this.adapter = leadAdapter
            this.addOnScrollListener(object : OnScrollListener() {
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
        binding.txtSeeall.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_viewleads)
        }
        binding.reminderNav.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addreminder)
        }
        /* if (PrefManager(requireContext()).userData?.IsAdmin != "1") {
             binding.layoutSubHeader.visibility = View.GONE
         }*/
        getProperties()
        getProperty()
        getLeads(assignTo)
        getReminders()
        binding.swipeLead.setOnRefreshListener {
            getLeads(assignTo)
        }
        return root
    }

    private fun getReminders() {
        homeViewModel?.getLeadSchedule(
            prefmanager.userData?.UserName!!,
            currentonlydate() /*"2023-11-03"*/,
            success = {
                if (!it.leadscheduleList.isNullOrEmpty()) showReminders(
                    it.leadscheduleList,
                    clickListener = {
                        homeViewModel?.getLeadData(it.contactnumber!!, success = {
                            if (it.leadsList.isNotEmpty()) {
                                val bundle = Bundle()
                                bundle.putString(LEAD_DATA, Gson().toJson(it.leadsList.first()))
                                findNavController().navigate(
                                    R.id.action_homeFragment_to_leadinfo,
                                    bundle
                                )
                            }
                        }, error = {})
                    })
            },
            error = {})
    }

    private fun showReminders(
        leadscheduleList: ArrayList<LeadscheduleList>,
        clickListener: (LeadscheduleList) -> Unit
    ) {
        val dialog = Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_employee);
        val linearlayout = dialog.findViewById<LinearLayout>(R.id.employeeList)
        dialog.findViewById<AppCompatTextView>(R.id.titleHeader).apply {
            text = "Today's Lead Reminders"
        }
        linearlayout.removeAllViews()
        leadscheduleList.forEachIndexed { index, userDetailsList ->
            linearlayout.addView(
                layoutInflater.inflate(R.layout.item_employee, null)?.apply {
                    this.findViewById<AppCompatTextView>(R.id.textName).apply {
                        text = userDetailsList.getData()
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

    private fun getProperties() {
        homeViewModel?.getProperties("", success = {
            propertyData = it
        }, error = {})
    }

    private fun getUsers(leadData: LeadsList) {
        homeViewModel?.getUsers(success = {
            it.userDetailsList.let { usersList ->
                val employeeList = usersList.filter { it.IsAdmin == "2" }
                showEmployeeDialog(employeeList) {
                    leadData.assignto = it.UserName
                    homeViewModel?.updateLead(leadsList = leadData, success = {
                        getLeads(assignTo)
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

    private fun getProperty() {
        homeViewModel?.getProperty(success = {
            it.propertyList.let { propertyList ->
                if (prefmanager.userData?.IsAdmin == "1") {
                    binding.txtPropertyCount.setText(it.propertyList.size.toString())
                } else {
                    val filter =
                        it.propertyList.filter { it.assignto == prefmanager.userData?.UserName }
                    binding.txtPropertyCount.setText(filter.size.toString())

                }
            }
        }, error = {

        })
    }

    private fun getLeads(assignTo: String? = "") {
        binding.animationView.visibility = View.VISIBLE
        binding.leadsList.visibility = View.GONE
        homeViewModel?.getLeads(assignTo!!, success = {
            viewLifecycleOwner.lifecycleScope.launch {
                if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
                delay(1500)
                binding.animationView.visibility = View.GONE
                binding.leadsList.visibility = View.VISIBLE
                if (it.leadsList.isNotEmpty()) {
                    binding.txtLeadsCount.setText(it.leadsList.size.toString())
                    leadAdapter?.updateList(it.leadsList, propertyData?.propertyList ?: emptyList())
                } else leadAdapter?.updateList(emptyList(), emptyList())
                try {
                    binding.leadsList.smoothScrollToPosition(prefmanager.listPos)
                    prefmanager.listPos = 0
                } catch (exp: Exception) {
                    //do nothing
                }
            }
        }, error = {
            if (binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing = false
            binding.animationView.visibility = View.GONE
        })
    }

    override fun onAttach(context: Context) {
        (activity as MainActivity).setTitle("Dashboard")
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun LeadscheduleList.getData(): String? {
        return "Name : ${this.leadsname}\nNumber : ${this.contactnumber}"
    }

    private fun setStatus(leadData: LeadsList) {
        showStatusDialog(leadData) { it, feedback ->
            leadData.status = it
            leadData.feedback = feedback
            homeViewModel?.updateLead(leadData, success = {
                getLeads(assignTo)
            }, error = {})
        }
    }

    fun showStatusDialog(
        leadData: LeadsList,
        clickListener: (String, String) -> Unit
    ) {
        var selectedStatus = ""
        val statusList = arrayListOf<String>(
            "Interested",
            "Not Interested",
            "Low Budget",
            "Junk Lead",
            "Confirmed",
            "Site Visit Done"
        )
        val dialog = Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_employee);
        val titleHeader = dialog.findViewById<AppCompatTextView>(R.id.titleHeader)
        val edtAddNotes = dialog.findViewById<AppCompatEditText>(R.id.edtAddNotes)
        val btnUpdate = dialog.findViewById<AppCompatButton>(R.id.btnUpdate)
        titleHeader.setText("Select Status")
        edtAddNotes.visibility = View.VISIBLE
        btnUpdate.visibility = View.VISIBLE
        val linearlayout = dialog.findViewById<LinearLayout>(R.id.employeeList)
        linearlayout.removeAllViews()
        statusList.forEachIndexed { index, userDetailsList ->
            linearlayout.addView(
                layoutInflater.inflate(R.layout.item_employee, null)?.apply {
                    this.findViewById<AppCompatTextView>(R.id.textName).apply {
                        text = userDetailsList
                        setOnClickListener {
                            linearlayout.updateViews(index)
                            selectedStatus = userDetailsList
                        }
                    }

                }
            )
        }
        btnUpdate.setOnClickListener {
            if (!selectedStatus.isNullOrEmpty()) {
                clickListener.invoke(selectedStatus, edtAddNotes.text.toString().trim())
                dialog.dismiss()
            } else showToast(requireContext(), "please select status")
        }
        dialog.setCancelable(true)
        dialog.show();

    }

    fun LinearLayout.updateViews(selectedindex: Int) {
        this.children.forEachIndexed { index, view ->
            view.findViewById<AppCompatTextView>(R.id.textName).apply {
                if (index == selectedindex) {
                    this.setBackgroundColor(resources.getColor(R.color.c_site_visit_done))
                } else {
                    this.setBackgroundColor(resources.getColor(R.color.white))
                }
            }
        }
    }
}


