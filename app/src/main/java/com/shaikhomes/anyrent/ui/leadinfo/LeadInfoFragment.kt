package com.shaikhomes.anyrent.ui.leadinfo

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.jraska.falcon.Falcon
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.FragmentLeadinfoBinding
import com.shaikhomes.anyrent.ui.customviews.SafeClickListener
import com.shaikhomes.anyrent.ui.models.LeadsList
import com.shaikhomes.anyrent.ui.models.PropertyList
import com.shaikhomes.anyrent.ui.utils.LEAD_DATA
import com.shaikhomes.anyrent.ui.utils.PROPERTY_DATA
import java.io.ByteArrayOutputStream
import java.net.URLEncoder


class LeadInfoFragment : Fragment() {
    private var _binding: FragmentLeadinfoBinding? = null
    private val binding get() = _binding!!
    private var leadsList: LeadsList? = null
    private var leadInfoViewModel: LeadInfoViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeadinfoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        leadInfoViewModel =
            ViewModelProvider(this).get(LeadInfoViewModel::class.java)
        if (!arguments?.getString(LEAD_DATA).isNullOrEmpty()) {
            leadsList = Gson().fromJson(arguments?.getString(LEAD_DATA), LeadsList::class.java)
        }
        //updateLeadData(leadsList)
        binding.btmAddReminder.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(LEAD_DATA, Gson().toJson(leadsList))
            findNavController().navigate(
                R.id.action_leadinfoFragment_to_addreminder,
                bundle
            )
        }
        return root
    }

    private fun updateLeadData(leadsList: LeadsList?) {
        leadsList.let { leadData ->
            if (!leadData?.contactnumber.isNullOrEmpty()) {
                binding.txtLeadNameName.setText(leadData?.leadsname)
                binding.leadName.setText(leadData?.leadsname)
                val lookingfor = if (leadData?.lookingfor?.toLowerCase() == "male") {
                    Html.fromHtml("<font color='#0a45d0'>${leadData.lookingfor}</font>")
                } else if (leadData?.lookingfor?.toLowerCase() == "female") {
                    Html.fromHtml("<font color='#e75480'>${leadData.lookingfor}</font>")
                } else if (leadData?.lookingfor?.toLowerCase() == "family") {
                    Html.fromHtml("<font color='#644117'>${leadData.lookingfor}</font>")
                } else if (leadData?.lookingfor?.toLowerCase() == "couples") {
                    Html.fromHtml("<font color='#FF0000'>${leadData.lookingfor}</font>")
                } else ""
                binding.LookingFor.setText(lookingfor, TextView.BufferType.SPANNABLE)
                binding.contactNo.setText(
                    (if (!leadData?.countrycode.isNullOrEmpty()) leadData?.countrycode else "").plus(
                        leadData?.contactnumber
                    )
                )
                binding.source.setText(leadData?.createdby)
                val priority = when (leadData?.priority) {
                    "High" -> {
                        "<font color='red'>${leadData.priority?.toUpperCase()}</font>"
                    }

                    "Medium" -> {
                        "<font color='#FFA500'>${leadData.priority?.toUpperCase()}</font>"
                    }

                    "Low" -> {
                        "<font color='green'>${leadData.priority?.toUpperCase()}</font>"
                    }

                    else -> ""
                }
                binding.priority.setText(Html.fromHtml(priority), TextView.BufferType.SPANNABLE)
                binding.callImg.setOnClickListener {
                    try {
                        val intent = Intent(
                            Intent.ACTION_CALL,
                            Uri.parse(
                                "tel:" + (leadData?.countrycode ?: "+971") + leadData?.contactnumber
                            )
                        )
                        requireActivity().startActivity(intent)
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
                }
                binding.whatsappImg.setOnClickListener {
                    val packageManager = requireActivity().packageManager
                    val i = Intent(Intent.ACTION_VIEW)

                    try {
                        val url =
                            "https://api.whatsapp.com/send?phone=${(if (!leadData?.countrycode.isNullOrEmpty()) leadData?.countrycode else "+971") + leadData?.contactnumber}" + "&text=" + URLEncoder.encode(
                                "",
                                "UTF-8"
                            )
                        i.setPackage("com.whatsapp.w4b")
                        i.data = Uri.parse(url)
                        requireActivity().startActivity(i)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                binding.shareImg.setOnClickListener {
                    val packageManager = requireActivity().packageManager
                    val i = Intent(Intent.ACTION_VIEW)

                    try {
                        val url =
                            "https://api.whatsapp.com/send?phone=${(if (!leadData?.countrycode.isNullOrEmpty()) leadData?.countrycode else "+971") + leadData?.contactnumber}" + "&text=" + URLEncoder.encode(
                                "Kindly schedule your visit!!\n" +
                                        "https://shaikhomes.com/addreminder/schedule.html",
                                "UTF-8"
                            )
                        i.setPackage("com.whatsapp.w4b")
                        i.data = Uri.parse(url)
                        requireActivity().startActivity(i)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                binding.msgImg.setOnClickListener {
                    requireActivity().startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.fromParts("sms", leadData?.contactnumber, null)
                        )
                    )
                }
                binding.txtAddRequirement.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(LEAD_DATA, Gson().toJson(leadData))
                    findNavController().navigate(
                        R.id.action_leadinfoFragment_to_addrequirement,
                        bundle
                    )
                }
                binding.txtBasicInfo.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(LEAD_DATA, Gson().toJson(leadData))
                    findNavController().navigate(R.id.action_leadinfoFragment_to_addlead, bundle)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!leadsList?.contactnumber.isNullOrEmpty()) {
            getLeadDetails(leadsList?.contactnumber)
            getProperty(leadsList?.contactnumber)
            getLeadSchedules(leadsList?.contactnumber)
        }
    }

    private fun getProperty(contactnumber: String?) {
        leadInfoViewModel?.getProperties(contactnumber!!, success = {
            if (it.propertyList.isNotEmpty()) {
                updateProperties(it.propertyList.first())
            }
        }, error = {})
    }

    private fun getLeadSchedules(contactnumber: String?) {
        leadInfoViewModel?.getLeadSchedule(contactnumber!!, success = {
            if (it.leadscheduleList.isNotEmpty()) {
                binding.leadScheduleContainer.removeAllViews()
                it.leadscheduleList.forEach { scheduledData ->
                    binding.leadScheduleContainer.addView(
                        layoutInflater.inflate(R.layout.item_leadschedule, null)?.apply {
                            this.findViewById<AppCompatTextView>(R.id.scheduledOn).apply {
                                text = scheduledData.scheduledon
                            }
                            this.findViewById<AppCompatTextView>(R.id.leadActivity).apply {
                                text = scheduledData.activity
                            }
                            this.findViewById<AppCompatTextView>(R.id.notes).apply {
                                text = scheduledData.feedback
                            }

                        }
                    )
                }
            }
        }, error = {})
    }

    private fun updateProperties(propertyData: PropertyList) {
        binding.requirementGroup.visibility = View.VISIBLE
        binding.txtAddRequirement.visibility = View.GONE
        binding.leadType.text = propertyData.typeoflead
        binding.propertyType.text = propertyData.propertytype
        binding.subPropertyType.text = propertyData.subpropertytype
        binding.project.text = propertyData.project
        binding.localities.text = propertyData.locations
        binding.NoOfBedrooms.text = propertyData.noofbedrooms
        binding.budget.text = "₹${propertyData.minamount} - ₹${propertyData.maxamount}"
        binding.txtRequirements.setOnClickListener(SafeClickListener {
            val bundle = Bundle()
            bundle.putString(PROPERTY_DATA, Gson().toJson(propertyData))
            bundle.putString(LEAD_DATA, Gson().toJson(leadsList))
            findNavController().navigate(
                R.id.action_leadinfoFragment_to_addrequirement,
                bundle
            )
        })
    }

    private fun getLeadDetails(contactnumber: String?) {
        leadInfoViewModel?.getLeads(contactnumber!!, success = {
            if (it.leadsList.isNotEmpty()) {
                leadsList = it.leadsList.first()
                updateLeadData(leadsList)
            }
        }, error = {

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_logout).setVisible(false)
        menu.findItem(R.id.action_broadcast).setVisible(false)
        menu.findItem(R.id.action_logout).actionView?.visibility = View.GONE
        menu.findItem(R.id.action_delete).setVisible(true)
        menu.findItem(R.id.action_screenshot).setVisible(true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(requireContext()).apply {
                this.setMessage("Do you want to delete ${leadsList?.leadsname}?")
                this.setPositiveButton(
                    "YES"
                ) { p0, p1 ->
                    deleteLead()
                }
                this.setNegativeButton(
                    "NO"
                ) { p0, p1 ->
                    p0.dismiss()
                }
                this.setCancelable(true)
                this.show()

            }
        } else if (item.itemId == R.id.action_screenshot) {
            val bitmap = Falcon.takeScreenshotBitmap(requireActivity())
            shareImage(bitmap, leadsList?.leadsname)
        }
        return super.onOptionsItemSelected(item)
    }

    //pass your image and text(if you want to share) in this method.
    fun shareImage(bitmap: Bitmap?, text: String?) {
        try {
            val pathofBmp = MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                bitmap, "title", null
            )
            val uri = Uri.parse(pathofBmp)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Star App")
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(shareIntent, "Smart Diary"))
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }

    fun onClickApp(pack: String?, bitmap: Bitmap) {
        val pm = requireContext().packageManager
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "SmartDiary",
                null
            )
            val imageUri = Uri.parse(path)
            @Suppress("unused") val info = pm.getPackageInfo(pack!!, PackageManager.GET_META_DATA)
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/*"
            waIntent.setPackage(pack)
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            waIntent.putExtra(Intent.EXTRA_TEXT, pack)
            requireContext().startActivity(Intent.createChooser(waIntent, "Share with"))
        } catch (e: java.lang.Exception) {
            Log.e("Error on sharing", "$e ")
            Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLead() {
        leadsList?.delete = "delete"
        leadsList?.update = null
        leadInfoViewModel?.deleteLead(leadsList!!, success = {
            Toast.makeText(requireContext(), "Lead Deleted Successfully", Toast.LENGTH_SHORT)
                .show()
            requireActivity().onBackPressed()
        }, error = {

        })
    }
}
