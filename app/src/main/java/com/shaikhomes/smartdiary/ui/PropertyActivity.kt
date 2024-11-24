package com.shaikhomes.smartdiary.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityPropertyBinding
import com.shaikhomes.smartdiary.ApartmentActivity
import com.shaikhomes.smartdiary.LoginActivity
import com.shaikhomes.smartdiary.ui.adapters.ApartmentAdapter
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.Beds
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.hideKeyboard
import com.shaikhomes.smartdiary.ui.utils.showToast
import kotlinx.coroutines.launch

class PropertyActivity : AppCompatActivity() {
    private lateinit var activityPropertyBinding: ActivityPropertyBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentAdapter: ApartmentAdapter? = null
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPropertyBinding = ActivityPropertyBinding.inflate(layoutInflater)
        setContentView(activityPropertyBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Properties"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityPropertyBinding.addProperty.setOnClickListener {
            showAddPropertyDialog()
        }
        apartmentAdapter = ApartmentAdapter(this, arrayListOf()).apply {
            setAvailableClickListener { apartmentList, appCompatTextView ->
                bindRoomsData(apartmentList, appCompatTextView)
            }
            setEditClickListener {

            }
            setPropertyClickListener {

                val intent = Intent(this@PropertyActivity, ApartmentActivity::class.java)
                intent.putExtra("apartment", Gson().toJson(it))
                startActivity(intent)
            }

            setInfoClickListener {
                prefmanager.selectedApartment = it
                onBackPressed()
            }
        }
        activityPropertyBinding.propertyList.apply {
            layoutManager = LinearLayoutManager(this@PropertyActivity)
            adapter = apartmentAdapter
        }
        getApartments()
    }

    private fun bindRoomsData(apartmentList: ApartmentList, appCompatTextView: AppCompatTextView) {
        addApartmentViewModel?.getRooms(success = {
            bindRoomData(it.roomsList, appCompatTextView)
        }, error = {
            showToast(this, it)
        }, apartmentid = apartmentList.ID.toString(), floorno = "", flatno = "")
    }

    suspend fun ArrayList<RoomData.RoomsList>.getCapacity(capacity: (Int) -> Unit) {
        var count: Int = 0
        this.forEach {
            if (it?.available != null) {
                if (!it.available.isNullOrEmpty()) {
                    val list: ArrayList<Beds> = Gson().fromJson(it?.available, bedsType)
                    list.forEach { bed ->
                        if (bed.userId.isNullOrEmpty()) {
                            count += 1
                        }
                    }
                }
            }
        }
        capacity.invoke(count)
    }

    private fun bindRoomData(
        roomsList: ArrayList<RoomData.RoomsList>,
        txtBedsCount: AppCompatTextView
    ) {
        lifecycleScope.launch {
            if (roomsList.isNotEmpty()) {
                roomsList.getCapacity { tot ->
                    txtBedsCount.setText(
                        Html.fromHtml("Available: <font color='#000E77'>${tot}</font>"),
                        TextView.BufferType.SPANNABLE
                    )
                }
            } else {
                txtBedsCount.setText(
                    Html.fromHtml("Available: <font color='#000E77'>${0}</font>"),
                    TextView.BufferType.SPANNABLE
                )
            }
        }
    }

    private fun showAddPropertyDialog() {
        val listFor: ArrayList<String>? = arrayListOf()
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_property, null)
        listFor.addFor()
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val spinnerFor = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextNoOfFloors = view.findViewById<EditText>(R.id.editTextNoOfFloors)
        val editTextAddress = view.findViewById<EditText>(R.id.editTextAddress)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        editTextAddress.visibility = View.VISIBLE
        spinnerFor.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, listFor!!
        )
        spinnerFor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFor = if (selectedItem != "Select") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        submitButton.setOnClickListener {
            val name = editTextName.text.toString()
            val noOfFloors = editTextNoOfFloors.text.toString()
            val address = editTextAddress.text.toString()
            if (name.isBlank() || selectFor.isBlank() || noOfFloors.isBlank() || address.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (noOfFloors == "0") {
                Toast.makeText(this, "Please add one floor", Toast.LENGTH_SHORT).show()
            } else {
                val floor = arrayListOf(noOfFloors)
                hideKeyboard(it)
                val apartmentData = ApartmentList(
                    userid = prefmanager.userData?.UserId.toString(),
                    apartmentname = name.trim(),
                    apartmentfor = selectFor,
                    nooffloors = Gson().toJson(floor),
                    createdby = prefmanager.userData?.UserName,
                    address = address,
                    updatedon = currentdate()
                )
                addApartmentViewModel?.addApartment(apartmentData, success = {
                    if (it.status == "200") {
                        editTextName.setText("")
                        editTextNoOfFloors.setText("")
                        getApartments()
                    }
                    bottomSheetDialog.dismiss()
                }, error = {
                    showToast(this, it)
                    bottomSheetDialog.dismiss()
                })
            }
        }
        bottomSheetDialog.behavior.peekHeight = 900
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
    }

    private fun getApartments() {
        addApartmentViewModel?.getApartments(success = {
            apartmentAdapter?.updateList(it.apartmentList)
        }, error = {
            showToast(this, it)
        }, userid = prefmanager.userData?.UserId.toString(), apartmentid = "")
    }

    private fun java.util.ArrayList<String>?.addFor() {
        this?.add("Select")
        this?.add("male")
        this?.add("female")
        this?.add("family")
        this?.add("co-living")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            AlertDialog.Builder(this).apply {
                this.setMessage("Do you want to logout?")
                this.setPositiveButton(
                    "YES"
                ) { p0, p1 ->
                    prefmanager.isLoggedIn = false
                    prefmanager.userData = null
                    p0.dismiss()
                    startActivity(Intent(this@PropertyActivity, LoginActivity::class.java))
                    finish()
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
        return super.onOptionsItemSelected(item)
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }
}