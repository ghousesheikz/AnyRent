package com.shaikhomes.smartdiary.ui.addreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arkapp.iosdatettimepicker.ui.DialogDateTimePicker
import com.arkapp.iosdatettimepicker.utils.OnDateTimeSelectedListener
import com.ozcanalasalvar.library.utils.DateUtils
import com.ozcanalasalvar.library.view.popup.DatePickerPopup
import com.ozcanalasalvar.library.view.popup.TimePickerPopup
import com.ozcanalasalvar.library.view.timePicker.TimePicker
import com.ozcanalasalvar.library.view.datePicker.DatePicker;
import com.shaikhomes.smartdiary.R
import com.shaikhomes.smartdiary.databinding.FragmentAddreminderBinding
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.utils.LEAD_DATA
import com.shaikhomes.smartdiary.ui.utils.PrefManager
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addRequirementViewModel =
            ViewModelProvider(this).get(AddReminderViewModel::class.java)
        if (!arguments?.getString(LEAD_DATA).isNullOrEmpty()) {
            // leadsList = Gson().fromJson(arguments?.getString(LEAD_DATA), LeadsList::class.java)
        }
        _binding = FragmentAddreminderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.btnSelectDate.setOnClickListener(clickListener)
        binding.btnSelectTime.setOnClickListener(clickListener)
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
        }
    }

    private fun selectDate() {
        /*DatePickerPopup.Builder()
            .from(requireContext())
            .offset(3)
            .pickerMode(DatePicker.DAY_ON_FIRST)
            .textSize(19)
            .endDate(DateUtils.getTimeMiles(2050, 10, 25))
            .currentDate(DateUtils.getCurrentTime())
            .startDate(DateUtils.getCurrentTime())
            .listener { dp, date, day, month, year ->
                binding.txtDate.text = "$year-$month-$day"
            }
            .build().show()*/
        val startDate: Calendar = Calendar.getInstance()

        val dateTimeSelectedListener = object :

            OnDateTimeSelectedListener {
            override fun onDateTimeSelected(selectedDateTime: Calendar) {
                //This is the calendar reference of selected date and time.
                //We can format the date time as we need here.
                println("Selected date ${selectedDateTime.time}")

            }

        }
        DialogDateTimePicker(
            requireContext(), //context
            startDate, //start date of calendar
            12, //No. of future months to shown in calendar
            dateTimeSelectedListener,
            "Select date and time").show()
    }

    private fun selectTime() {
        val timePicker = TimePickerPopup.Builder()
            .from(requireContext())
            .offset(3)
            .textSize(17)
            .setTime(DateUtils.getCurrentHour(), DateUtils.getCurrentMinute())
            .listener { timePicker, hour, minute ->
                binding.txtDate.text = "$hour:$minute"
            }
            .build().show()
    }

}
