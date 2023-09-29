package com.shaikhomes.smartdiary.ui.propertylist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentPropertydataBinding
import com.shaikhomes.smartdiary.ui.models.PropertyList
import com.shaikhomes.smartdiary.ui.models.UserDetailsList
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import kotlinx.coroutines.launch

class PropertyDataFragment : Fragment() {
    private var _binding: FragmentPropertydataBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var viewLeadsViewModel: PropertyDataViewModel? = null
    private var leadAdapter: PropertyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPropertydataBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewLeadsViewModel = ViewModelProvider(this).get(PropertyDataViewModel::class.java)

        binding.employeeList.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            leadAdapter = PropertyAdapter(
                requireContext(),
                arrayListOf(),
                isAdmin = PrefManager(requireContext()).userData?.IsAdmin == "1"
            )
            leadAdapter?.setLeadClickListener {
                //updateEmployee(it)
            }
            leadAdapter?.setAssignToClickListener {
                it.updatedon = currentdate()
                it.update = "update"
                getUsers(it)
            }
            this.adapter = leadAdapter
        }
        getProperty()
        binding.swipeEmployee.setOnRefreshListener {
            getProperty()
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
                        getProperty()
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

    private fun getUsers(leadData: PropertyList) {
        viewLeadsViewModel?.getUsers(success = {
            it.userDetailsList.let { usersList ->
                val employeeList = usersList.filter { it.IsAdmin == "2" }
                showEmployeeDialog(employeeList) {
                    leadData.assignto = it.UserName
                    viewLeadsViewModel?.updateProperty(leadsList = leadData, success = {
                        getProperty()
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
        viewLeadsViewModel?.getProperty(success = {
            if (binding.swipeEmployee.isRefreshing) binding.swipeEmployee.isRefreshing = false
            it.propertyList.let { propertyList ->
                if (prefmanager.userData?.IsAdmin == "1") {
                    leadAdapter?.updateList(propertyList)
                } else {
                    val filter =
                        it.propertyList.filter { it.assignto == prefmanager.userData?.UserName }
                    leadAdapter?.updateList(filter)
                }
            }
        }, error = {
            if (binding.swipeEmployee.isRefreshing) binding.swipeEmployee.isRefreshing = false
        })
    }

}