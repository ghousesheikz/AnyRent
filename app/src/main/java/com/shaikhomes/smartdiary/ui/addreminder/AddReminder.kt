package com.shaikhomes.smartdiary.ui.addreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentAddreminderBinding
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.LeadsList
import com.shaikhomes.smartdiary.ui.models.LeadscheduleList
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import java.util.Calendar


class AddReminder : Fragment() {

    private var _binding: FragmentAddreminderBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var addRequirementViewModel: AddReminderViewModel? = null
    protected val prefmanager: PrefManager by lazy {
        PrefManager(requireContext())
    }
    private var leadsList: LeadsList? = null
    private var leadActivity: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addRequirementViewModel =
            ViewModelProvider(this).get(AddReminderViewModel::class.java)
        if (!arguments?.getString(LEAD_DATA).isNullOrEmpty()) {
            leadsList = Gson().fromJson(arguments?.getString(LEAD_DATA), LeadsList::class.java)
        }
        _binding = FragmentAddreminderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnSelectDate.setOnClickListener(clickListener)
        binding.btnSelectTime.setOnClickListener(clickListener)
        binding.txtCall.setOnClickListener(normalClickListener)
        binding.txtSitevisit.setOnClickListener(normalClickListener)
        binding.txtFacetoFace.setOnClickListener(normalClickListener)
        leadsList?.contactnumber.let {
            binding.edtLeadNumber.setText(leadsList?.contactnumber)
        }
        binding.btmAddReminder.setOnClickListener(clickListener)
        return root
    }

    private val clickListener = SafeClickListener {
        when (it?.id) {
            R.id.btnSelectDate -> {
                selectDate()
            }

            R.id.btnSelectTime -> {
                selectTime()
            }

            R.id.btmAddReminder -> {
                if (validations()) {
                    val scheduleon =
                        "${binding.txtDate.text.toString()} ${binding.txtTime.text.toString()}"
                    val leadScheduleList = LeadscheduleList(
                        contactnumber = binding.edtLeadNumber.text.toString().trim(),
                        activity = leadActivity,
                        leadsname = leadsList?.leadsname,
                        createdon = currentdate(),
                        scheduledon = scheduleon,
                        feedback = binding.edtAddNotes.text.toString().trim(),
                        createdby = prefmanager.userData?.UserName.toString(),
                        assignto = leadsList?.assignto,
                        updatedon = currentdate()
                    )
                    addRequirementViewModel?.scheduleVisit(leadScheduleList, success = {
                        emptyData()
                        Toast.makeText(
                            requireContext(),
                            "Lead Scheduled Successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        requireActivity().onBackPressed()
                    }, error = {

                    })
                }
            }

        }
    }

    private fun emptyData() {
        binding.edtLeadNumber.setText("")
        binding.txtDate.setText("")
        binding.txtTime.setText("")
        binding.edtAddNotes.setText("")
    }

    private fun validations(): Boolean {
        var flag: Boolean = true
        if (binding.edtLeadNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Enter Leads Number", Toast.LENGTH_SHORT).show()
        } else if (leadActivity?.isEmpty() == true) {
            flag = false
            Toast.makeText(requireContext(), "Please select lead activity", Toast.LENGTH_SHORT)
                .show()
        } else if (binding.txtDate.text.toString().isNullOrEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Please select date", Toast.LENGTH_SHORT).show()
        } else if (binding.txtTime.text.toString().isNullOrEmpty()) {
            flag = false
            Toast.makeText(requireContext(), "Please select time", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    private val normalClickListener = View.OnClickListener {
        when (it?.id) {


            R.id.txtCall -> {
                leadActivity = "Call"
                binding.txtCall.setBackgroundColor(resources.getColor(R.color.purple_500))
                binding.txtCall.setTextColor(resources.getColor(R.color.white))
                binding.txtSitevisit.setTextColor(resources.getColor(R.color.black))
                binding.txtFacetoFace.setTextColor(resources.getColor(R.color.black))
                binding.txtSitevisit.setBackgroundColor(resources.getColor(R.color.white))
                binding.txtFacetoFace.setBackgroundColor(resources.getColor(R.color.white))
            }

            R.id.txtSitevisit -> {
                leadActivity = "Site Visit"
                binding.txtSitevisit.setBackgroundColor(resources.getColor(R.color.purple_500))
                binding.txtSitevisit.setTextColor(resources.getColor(R.color.white))
                binding.txtCall.setTextColor(resources.getColor(R.color.black))
                binding.txtFacetoFace.setTextColor(resources.getColor(R.color.black))
                binding.txtCall.setBackgroundColor(resources.getColor(R.color.white))
                binding.txtFacetoFace.setBackgroundColor(resources.getColor(R.color.white))
            }

            R.id.txtFacetoFace -> {
                leadActivity = "Face to Face Discussion"
                binding.txtFacetoFace.setBackgroundColor(resources.getColor(R.color.purple_500))
                binding.txtFacetoFace.setTextColor(resources.getColor(R.color.white))
                binding.txtCall.setTextColor(resources.getColor(R.color.black))
                binding.txtSitevisit.setTextColor(resources.getColor(R.color.black))
                binding.txtCall.setBackgroundColor(resources.getColor(R.color.white))
                binding.txtSitevisit.setBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }

    private fun selectDate() {
        var calendar = Calendar.getInstance()
        val datePickerDialog = context?.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    binding.txtDate.text = "$year-${monthOfYear.plus(1)}-$dayOfMonth"

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()

    }

    private fun selectTime() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { view, hourOfDay, minute ->
                binding.txtTime.setText("$hourOfDay:$minute")
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

}
