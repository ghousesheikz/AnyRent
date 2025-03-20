package com.shaikhomes.anyrent

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.shaikhomes.anyrent.databinding.ActivityApartmentBinding
import com.shaikhomes.anyrent.ui.adapters.FlatAdapter
import com.shaikhomes.anyrent.ui.adapters.FloorsAdapter
import com.shaikhomes.anyrent.ui.adapters.RoomsAdapter
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.Capacity
import com.shaikhomes.anyrent.ui.models.FlatData
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.hideKeyboard
import com.shaikhomes.anyrent.ui.utils.showToast
import java.io.File
import java.io.FileOutputStream

class ApartmentActivity : AppCompatActivity() {
    private lateinit var activityApartmentBinding: ActivityApartmentBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList: ApartmentList? = null
    private var floorsAdapter: FloorsAdapter? = null
    private var flatAdapter: FlatAdapter? = null
    private var roomAdapter: RoomsAdapter? = null
    val type = object : TypeToken<ArrayList<String>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityApartmentBinding = ActivityApartmentBinding.inflate(layoutInflater)
        setContentView(activityApartmentBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Property Details"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        apartmentList =
            Gson().fromJson(intent.getStringExtra("apartment"), ApartmentList::class.java)
        activityApartmentBinding.apply {
            propertyName.text = apartmentList?.apartmentname
            floorsList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.HORIZONTAL, false)
            floorsAdapter = FloorsAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setFloorClickListener {
                    FloorClickListener(it)
                }
            }
            floorsList.adapter = floorsAdapter
            flatList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.HORIZONTAL, false)
            flatAdapter = FlatAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setFlatClickListener {
                    FlatClickListener(it)
                }
            }
            flatList.adapter = flatAdapter
            roomList.layoutManager =
                LinearLayoutManager(this@ApartmentActivity, LinearLayoutManager.VERTICAL, false)
            roomAdapter = RoomsAdapter(this@ApartmentActivity, arrayListOf(), true).apply {
                setDeleteClickListener { room ->
                    deleteRoom(room)
                }
                setBedClickListener { roomsList, beds ->
                    val intent = Intent(this@ApartmentActivity, TenantRegistration::class.java)
                    intent.putExtra("ROOM_SELECT", Gson().toJson(roomsList))
                    intent.putExtra("BED_SELECT", Gson().toJson(beds))
                    startActivity(intent)
                }
                setQRCodeClickListener { roomsList, beds ->
                    var encDetails = "${roomsList?.roomname} - B${beds?.number}"
                    encDetails = encDetails.replace(" ", "%20")
                    val url =
                        "https://myhotelsbooking.com/TenantRegistrationForm/?apartment=${roomsList.apartmentid}&floorno=${roomsList?.floorno}&roomno=${roomsList?.ID}&flatno=${roomsList?.flatno}&details=${encDetails}"
                    // Generate QR Code
                    val bitmap = generateQRCode(url)
                    if (bitmap != null) {
                        // Show QR code in a popup dialog
                        showQRCodeDialog(bitmap, url)
                    } else {
                        Toast.makeText(
                            this@ApartmentActivity,
                            "Failed to generate QR Code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                setTenantClickListener { roomsList, beds ->
                    addApartmentViewModel?.getTenants(success = { tenantList ->
                        if (tenantList.tenant_list.isNotEmpty()) {
                            val intent =
                                Intent(this@ApartmentActivity, TenantOverview::class.java)
                            intent.putExtra("tenant", Gson().toJson(tenantList.tenant_list.first()))
                            startActivity(intent)
                        }
                    }, error = {
                        showToast(this@ApartmentActivity, it)
                    }, beds.userId ?: "", "", "", "")
                }
            }
            roomList.adapter = roomAdapter
            addFlat.setOnClickListener {
                addFloorFlat()
                menuGreen.close(true)
            }
            addFloor.setOnClickListener {
                addPropertyFloor()
                menuGreen.close(true)
            }
            addRoom.setOnClickListener {
                addFlatRooms()
                menuGreen.close(true)
            }
        }
        getFloors()
    }

    private fun deleteRoom(room: RoomData.RoomsList) {
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to delete ${room.roomname}?")
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                room.update = "delete"
                addApartmentViewModel?.addRooms(room, success = {
                    if (it.status == "200") {
                        showToast(this@ApartmentActivity, "Room deleted successfully")
                        getApartments()
                    }
                }, error = {
                    showToast(this@ApartmentActivity, it)
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

    private fun addFlatRooms() {
        val floorList: ArrayList<String>? = arrayListOf()
        val capacityList: ArrayList<Capacity>? = arrayListOf()
        val roomTypeList: ArrayList<String>? = arrayListOf()
        var flatList: ArrayList<FlatData.FlatList>? = arrayListOf()
        flatList?.add(FlatData.FlatList(ID = -1, flatname = "Select Flat"))
        capacityList?.add(Capacity(name = "Select Room Capacity", number = "-1"))
        capacityList?.add(Capacity(name = "Single Sharing", number = "1"))
        capacityList?.add(Capacity(name = "Double Sharing", number = "2"))
        capacityList?.add(Capacity(name = "Triple Sharing", number = "3"))
        capacityList?.add(Capacity(name = "4 Sharing", number = "4"))
        capacityList?.add(Capacity(name = "5 Sharing", number = "5"))
        capacityList?.add(Capacity(name = "6 Sharing", number = "6"))
        capacityList?.add(Capacity(name = "7 Sharing", number = "7"))
        capacityList?.add(Capacity(name = "8 Sharing", number = "8"))
        capacityList?.add(Capacity(name = "9 Sharing", number = "9"))
        capacityList?.add(Capacity(name = "10 Sharing", number = "10"))
        capacityList?.add(Capacity(name = "11 Sharing", number = "11"))
        capacityList?.add(Capacity(name = "12 Sharing", number = "12"))
        capacityList?.add(Capacity(name = "13 Sharing", number = "13"))
        capacityList?.add(Capacity(name = "14 Sharing", number = "14"))
        capacityList?.add(Capacity(name = "15 Sharing", number = "15"))
        roomTypeList?.add("Select Room Type")
        roomTypeList?.add("Regular")
        roomTypeList?.add("Deluxe")
        roomTypeList?.add("Ac")
        roomTypeList?.add("Non-AC")
        floorList?.addAll(getFloorData())
        var selectFloor: String = ""
        var selectFlat: String = ""
        var selectRoomType: String = ""
        var selectCapacity: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_rooms, null)
        val floorSpinner = view.findViewById<Spinner>(R.id.floorSpinner)
        val editRoomName = view.findViewById<EditText>(R.id.editRoomName)
        val editRentPerDay = view.findViewById<EditText>(R.id.editRentPerDay)
        val editRentPerMonth = view.findViewById<EditText>(R.id.editRentPerMonth)
        val flatSpinner = view.findViewById<Spinner>(R.id.flatSpinner)
        val capacitySpinner = view.findViewById<Spinner>(R.id.capacitySpinner)
        val roomtypeSpinner = view.findViewById<Spinner>(R.id.roomtypeSpinner)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        floorSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, floorList!!
        )
        capacitySpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, capacityList!!
        )
        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFloor = if (selectedItem != "Select Floor") {
                    addApartmentViewModel?.getFlats(
                        success = {
                            flatList = arrayListOf()
                            flatList?.add(FlatData.FlatList(ID = -1, flatname = "Select Flat"))
                            it.flatList.forEach { flat ->
                                flatList?.add(flat)
                            }
                            flatSpinner.adapter = ArrayAdapter(
                                this@ApartmentActivity,
                                R.layout.spinner_item, flatList!!
                            )
                        },
                        error = {
                            showToast(this@ApartmentActivity, it)
                        },
                        userid = prefmanager.userData?.UserId.toString(),
                        apartmentid = apartmentList?.ID.toString(),
                        floorno = selectedItem
                    )
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        flatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFlat = if (selectedItem != "Select Flat") {
                    flatList?.get(p2)?.ID.toString()
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        capacitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectCapacity = if (selectedItem != "Select Room Capacity") {
                    capacityList?.get(p2)?.number.toString()
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        roomtypeSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, roomTypeList!!
        )
        roomtypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectRoomType = if (selectedItem != "Select Room Type") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        submitButton.setOnClickListener {
            val name = editRoomName.text.toString()
            val editRentPerDay = editRentPerDay.text.toString()
            val editRentPerMonth = editRentPerMonth.text.toString()
            if (name.isBlank() || selectFloor.isBlank() || selectFlat.isBlank() || selectRoomType.isBlank() || editRentPerDay.isBlank() || editRentPerMonth.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (editRentPerDay == "0" || editRentPerMonth == "0") {
                Toast.makeText(this, "Amount should not be 0", Toast.LENGTH_SHORT).show()
            } else if (selectCapacity.isBlank()) {
                Toast.makeText(this, "Room capacity should not be empty", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(it)
                val apartmentData = RoomData.RoomsList(
                    ID = apartmentList?.ID,
                    roomname = name,
                    apartmentid = apartmentList?.ID.toString(),
                    roomcapacity = selectCapacity,
                    roomtype = selectRoomType,
                    rentperday = editRentPerDay,
                    rentpermonth = editRentPerMonth,
                    floorno = selectFloor,
                    flatno = selectFlat,
                    createdby = prefmanager.userData?.UserName,
                    available = selectCapacity.getRoomBeds(),
                    updatedon = currentdate()
                )
                addApartmentViewModel?.addRooms(apartmentData, success = {
                    if (it.status == "200") {
                        editRoomName.setText("")
                        getApartments()
                    }
                    bottomSheetDialog.dismiss()
                }, error = {
                    showToast(this, it)
                    bottomSheetDialog.dismiss()
                })
            }
        }
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        bottomSheetDialog.show()
    }

    private fun addFloorFlat() {
        val listFor: ArrayList<String>? = arrayListOf()
        listFor?.addAll(getFloorData())
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_property, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val txtAddProperty = view.findViewById<TextView>(R.id.txtAddProperty)
        val spinnerFor = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextNoOfFloors = view.findViewById<EditText>(R.id.editTextNoOfFloors)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        txtAddProperty.text = "Add Flat"
        submitButton.text = "Add Flat"
        editTextName.hint = "Flat Name"
        editTextNoOfFloors.visibility = View.GONE
        spinnerFor.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, listFor!!
        )
        spinnerFor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectFor = if (selectedItem != "Select Floor") {
                    selectedItem
                } else ""
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        submitButton.setOnClickListener {
            val name = editTextName.text.toString()
            val noOfFloors = editTextNoOfFloors.text.toString()
            if (name.isBlank() || selectFor.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(it)
                val apartmentData = FlatData.FlatList(
                    ID = apartmentList?.ID,
                    userid = prefmanager.userData?.UserId.toString(),
                    apartmentid = apartmentList?.ID.toString(),
                    flatname = name,
                    floorno = selectFor,
                    createdby = prefmanager.userData?.UserName,
                    updatedon = currentdate()
                )
                addApartmentViewModel?.addFlat(apartmentData, success = {
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

    private fun getFloors() {
        if (apartmentList?.nooffloors != null) {
            if (!apartmentList?.nooffloors.isNullOrEmpty()) {
                val list: ArrayList<String> = Gson().fromJson(apartmentList?.nooffloors, type)
                floorsAdapter?.updateList(list)
                floorsAdapter?.notifyDataSetChanged()
                floorsAdapter?.clearSelection()
                flatAdapter?.clearSelection()
                roomAdapter?.clearSelection()
                flatAdapter?.updateList(arrayListOf())
                roomAdapter?.updateList(arrayListOf())
            }
        }
    }

    fun String.getRoomBeds(): String {
        val size = this.toInt()
        val bedsList = arrayListOf<Beds>()
        for (i in 0 until size) {
            bedsList.add(Beds(number = "${i + 1}", occupied = false, userId = ""))
        }
        return Gson().toJson(bedsList)
    }

    private fun addPropertyFloor() {
        val listFor: ArrayList<String>? = arrayListOf()
        apartmentList?.apartmentfor?.let { listFor?.add(it) }
        var selectFor: String = ""
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.add_property, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val txtAddProperty = view.findViewById<TextView>(R.id.txtAddProperty)
        val spinnerFor = view.findViewById<Spinner>(R.id.typeSpinner)
        val editTextNoOfFloors = view.findViewById<EditText>(R.id.editTextNoOfFloors)
        val submitButton = view.findViewById<Button>(R.id.submitButton)
        //editTextNoOfFloors.setText(apartmentList?.nooffloors)
        editTextName.setText(apartmentList?.apartmentname)
        txtAddProperty.text = "Update Property"
        submitButton.text = "Update"
        editTextName.isEnabled = false
        spinnerFor.isEnabled = false
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
            if (name.isBlank() || selectFor.isBlank() || noOfFloors.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard(it)
                val arrayList: ArrayList<String> = Gson().fromJson(apartmentList?.nooffloors, type)
                arrayList.add(noOfFloors)
                val apartmentData = ApartmentList(
                    ID = apartmentList?.ID,
                    userid = prefmanager.userData?.UserId.toString(),
                    apartmentname = name.trim(),
                    apartmentfor = selectFor,
                    nooffloors = Gson().toJson(arrayList),
                    createdby = prefmanager.userData?.UserName,
                    updatedon = currentdate(),
                    update = "update"
                )
                //Log.v("FLOOR_DATA",Gson().toJson(apartmentData))
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
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList = it.apartmentList.first()
                    getFloors()
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentList?.ID.toString()
        )
    }

    private fun RoomClickListener(room: RoomData.RoomsList) {
        showToast(this, "${room.roomname} - ${room.roomcapacity}")
        activityApartmentBinding.apply {
            roomData.visibility = View.VISIBLE
            txtRoomName.text = room.roomname
            container.removeAllViews()
            // Add multiple ImageViews horizontally
            val size = room.roomcapacity?.toInt()
            for (i in 1..size!!) { // Add 5 images as an example
                val imageView = createImageView()
                container.addView(imageView)
            }
        }
    }

    private fun createImageView(): ImageView {
        val imageView = ImageView(this)

        // Set image properties
        imageView.setImageResource(R.drawable.ic_bed) // Replace with your drawable resource
        imageView.layoutParams = LinearLayout.LayoutParams(
            100, // Width in pixels
            100  // Height in pixels
        ).apply {
            setMargins(15, 15, 15, 15) // Add some margin between views
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP // Optional: Adjust scaling

        return imageView
    }

    fun getFloorData(): ArrayList<String> {
        return Gson().fromJson(apartmentList?.nooffloors, type)
    }

    private fun FlatClickListener(flatList: FlatData.FlatList) {
        activityApartmentBinding.roomData.visibility = View.GONE
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomAdapter?.clearSelection()
                    roomAdapter?.updateList(it.roomsList)
                } else roomAdapter?.updateList(arrayListOf())
            },
            error = {
                showToast(this, it)
            },
            apartmentid = apartmentList?.ID.toString(),
            floorno = flatList.floorno!!,
            flatno = flatList.ID.toString()
        )
    }

    private fun FloorClickListener(floor: String) {
        activityApartmentBinding.roomData.visibility = View.GONE
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatAdapter?.clearSelection()
                    roomAdapter?.clearSelection()
                    flatAdapter?.updateList(it.flatList)
                    roomAdapter?.updateList(arrayListOf())
                } else {
                    flatAdapter?.updateList(arrayListOf())
                    roomAdapter?.updateList(arrayListOf())
                }
            },
            error = {
                showToast(this, it)
            },
            prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentList?.ID.toString(),
            floor
        )
    }

    private fun showQRCodeDialog(bitmap: Bitmap, url: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_qr_code)

        val imageView: ImageView = dialog.findViewById(R.id.dialogImageView)
        val shareButton: ImageView = dialog.findViewById(R.id.dialogShareButton)

        imageView.setImageBitmap(bitmap)

        // Share QR Code
        shareButton.setOnClickListener {
            shareQRCode(bitmap,url)
        }

        dialog.show()
    }

    private fun shareQRCode(bitmap: Bitmap?, url:String) {
        if (bitmap == null) {
            Toast.makeText(this, "No QR Code to share", Toast.LENGTH_SHORT).show()
            return
        }

        try {

            val tempUri = saveImageToCache(bitmap)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, tempUri)
                putExtra(Intent.EXTRA_TEXT, "Scan this QR code or visit: $url")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing QR Code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): android.net.Uri {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs() // Create folder if it doesn't exist
        val file = File(cachePath, "qr_code.png")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.close()
        return androidx.core.content.FileProvider.getUriForFile(this, "com.shaikhomes.anyrent.provider", file)
    }

    private fun generateQRCode(text: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}