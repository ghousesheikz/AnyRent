package com.shaikhomes.smartdiary.ui.home

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.gson.Gson
import com.shaikhomes.smartdiary.MainActivity
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentHomeBinding
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.PropertyData
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
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
        binding.propertyNav.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addproperty)
        }
        PrefManager(requireContext()).userData.let {
            assignTo = if (it?.IsAdmin == "1") "" else it?.UserName.toString()
            binding.txtUserName.text = it?.UserName
            binding.txtLocation.text = it?.Address
        }
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
                findNavController().navigate(R.id.action_homeFragment_to_leadinfo, bundle)
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
        getLeads(assignTo)
        binding.swipeLead.setOnRefreshListener {
            getLeads(assignTo)
        }
        return root
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

    private fun getLeads(assignTo: String? = "") {
        binding.animationView.visibility = View.VISIBLE
        binding.leadsList.visibility = View.GONE
        homeViewModel?.getLeads(assignTo!!, success = {
            viewLifecycleOwner.lifecycleScope.launch {
                if(binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing=false
                delay(3000)
                binding.animationView.visibility = View.GONE
                binding.leadsList.visibility = View.VISIBLE
                if (it.leadsList.isNotEmpty()) {
                    binding.txtLeadsCount.setText(it.leadsList.size.toString())
                    leadAdapter?.updateList(it.leadsList, propertyData?.propertyList ?: emptyList())
                } else leadAdapter?.updateList(emptyList(), emptyList())
            }
        }, error = {
            if(binding.swipeLead.isRefreshing) binding.swipeLead.isRefreshing=false
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
}