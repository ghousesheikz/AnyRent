package com.shaikhomes.smartdiary.ui.employeeData

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shaikhomes.smartdiary.LoginActivity
import com.shaikhomes.smartdiary.databinding.FragmentEmployeedataBinding
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import kotlinx.coroutines.launch

class EmployeeDataFragment : Fragment() {
    private var _binding: FragmentEmployeedataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var viewLeadsViewModel: EmployeeDataViewModel? = null
    private var leadAdapter: EmployeeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployeedataBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewLeadsViewModel = ViewModelProvider(this).get(EmployeeDataViewModel::class.java)

        binding.employeeList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            leadAdapter = EmployeeAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            leadAdapter?.setLeadClickListener {
                updateEmployee(it)
            }
            this.adapter = leadAdapter
        }
        getEmployees()
        binding.swipeEmployee.setOnRefreshListener {
            getEmployees()
        }
        return root
    }

    private fun updateEmployee(userDetailsList: UserDetailsList) {
        AlertDialog.Builder(requireContext()).apply {
            this.setMessage("Do you want to ${if (userDetailsList.Active == "1") "Deactivate" else "Active"} this employee?")
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                userDetailsList.Active = if (userDetailsList.Active == "1") "0" else "1"
                userDetailsList.update = "update"
                viewLeadsViewModel?.postUsers(userDetailsList, success = {
                    viewLifecycleOwner.lifecycleScope.launch {
                        getEmployees()
                    }
                }, error = {

                })
            }
            this.setNegativeButton(
                "NO"
            ) { p0, p1 ->
                p0.dismiss()
            }
            this.setCancelable(true)
            this.show()
        }

    }

    private fun getLeads(userList: List<UserDetailsList>) {
        viewLeadsViewModel?.getLeads("", "", success = {
            viewLifecycleOwner.lifecycleScope.launch {
                if (!it.leadsList.isNullOrEmpty()) {
                    userList.forEach { userData ->
                        it.leadsList.filter { it.assignto == userData.UserName }.let {
                            if (!it.isNullOrEmpty()) {
                                userData.leadsCount = it.size
                            } else {
                                userData.leadsCount = 0
                            }
                        }
                    }

                    leadAdapter?.updateList(userList.sortedByDescending { it.leadsCount })

                } else {
                    leadAdapter?.updateList(userList)
                }
            }
        }, error = {

        })
    }


    private fun getEmployees() {
        viewLeadsViewModel?.getUsers(success = {
            if (binding.swipeEmployee.isRefreshing) binding.swipeEmployee.isRefreshing = false
            it.userDetailsList.let { usersList ->
                val employeeList = usersList.filter { it.IsAdmin == "2" }
                getLeads(employeeList)
            }
        }, error = {
            if (binding.swipeEmployee.isRefreshing) binding.swipeEmployee.isRefreshing = false
        })
    }

}