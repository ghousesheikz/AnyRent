package com.shaikhomes.smartdiary

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityTenantsBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.ApartmentList
import com.shaikhomes.smartdiary.ui.models.Beds
import com.shaikhomes.smartdiary.ui.models.FlatData
import com.shaikhomes.smartdiary.ui.models.ImageData
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.RoomData
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.FileUtil
import com.shaikhomes.smartdiary.ui.utils.ImagePicker
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
import com.shaikhomes.smartdiary.ui.utils.getCountryList
import com.shaikhomes.smartdiary.ui.utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TenantsActivity : AppCompatActivity() {

    private lateinit var activityTenantsBinding: ActivityTenantsBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var base64String: String? = ""
    private val requestCodeCameraPermission = 101
    private val requestCodeStoragePermission = 102
    private var photoUri: Uri? = null
    var file: File? = null
    var imagePath = ""
    val type = object : TypeToken<ArrayList<String>>() {}.type
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var apartmentList = arrayListOf<ApartmentList>()
    private var floorList = arrayListOf<String>()
    private var flatList = arrayListOf<FlatData.FlatList>()
    private var roomList = arrayListOf<RoomData.RoomsList>()
    private var genderist = arrayListOf<String>()
    private var bedsList = arrayListOf<Beds>()
    private var apartmentSelected: ApartmentList? = null
    private var bedSelected: Beds? = null
    private var FlatSelected: FlatData.FlatList? = null
    private var roomSelected: RoomData.RoomsList? = null
    private var selectApart: String? = null
    private var selectFloor: String? = null
    private var selectFlat: String? = null
    private var selectRoom: String? = null
    private var selectBed: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantsBinding = ActivityTenantsBinding.inflate(layoutInflater)
        setContentView(activityTenantsBinding.root)
        supportActionBar?.title = "Tenants"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        apartmentList.add(ApartmentList(ID = -1, apartmentname = "Select Apartment"))
        floorList.add("Select Floor")
        flatList.add(FlatData.FlatList(ID = -1, flatname = "Select Flat"))
        roomList.add(RoomData.RoomsList(ID = -1, roomname = "Select Room"))
        genderist.add("male")
        genderist.add("female")
        bedsList.add(Beds(number = "Available Beds"))
        activityTenantsBinding.apartmentSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, apartmentList!!
        )
        activityTenantsBinding.apartmentSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectApart = if (selectedItem != "Select Apartment") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activityTenantsBinding.floorSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, floorList!!
        )
        activityTenantsBinding.floorSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectFloor = if (selectedItem != "Select Floor") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activityTenantsBinding.flatSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, flatList!!
        )
        activityTenantsBinding.flatSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectFlat = if (selectedItem != "Select Flat") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activityTenantsBinding.roomSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, roomList!!
        )
        activityTenantsBinding.roomSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectRoom = if (selectedItem != "Select Room") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activityTenantsBinding.genderSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, genderist!!
        )
        activityTenantsBinding.bedsSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, bedsList!!
        )
        activityTenantsBinding.bedsSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    selectBed = if (selectedItem != "Available Beds") {
                        selectedItem
                    } else ""
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        activityTenantsBinding.editCheckIn.setOnClickListener {
            selectCheckInDate(activityTenantsBinding.editCheckIn)
        }
        activityTenantsBinding.editCheckOut.setOnClickListener {
            selectcheckOutDate(activityTenantsBinding.editCheckOut)
        }
        getApartments()
        activityTenantsBinding.addTenant.setOnClickListener(safeClickListener)
        activityTenantsBinding.textDropDownChooseCountry.setOnClickListener {
            try {
                val dialog =
                    CountrySelectionDialog.newInstance(
                        listener = { selectedCountry ->
                            activityTenantsBinding.textDropDownChooseCountry.text =
                                selectedCountry.dial_code
                        },
                        list = getCountryList(this)
                    )
                try {
                    val fragmentTransaction =
                        supportFragmentManager?.beginTransaction()
                    fragmentTransaction?.add(dialog, null)
                    fragmentTransaction?.commitAllowingStateLoss()
                } catch (exception: IllegalStateException) {
                    // do nothing
                }
            } catch (exception: IllegalStateException) {
                // do nothing
            }
        }
        activityTenantsBinding?.userImage?.setOnClickListener {
            selectImage()
        }
    }

    private fun selectImage() {
        BottomSheetImagePicker.showBottomSheet(
            this,
            object : BottomSheetImagePicker.ImagePickerBottomSheetClickListener {
                override fun imageClicked() {
                    pickImageFromGallery()
                }

                override fun cameraClicked() {
                    captureImage()
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE &&
            resultCode == Activity.RESULT_OK
        ) {
            val imageUri = ImagePicker.parseImageUri(this, data) ?: Uri.EMPTY
            FileUtil.getPath(this, imageUri).let {
                file = File(it)
                if (!file?.absolutePath.isNullOrEmpty()) {
                    activityTenantsBinding?.userImage?.setImageBitmap(BitmapFactory.decodeFile(file?.absolutePath))
                }
            }

        }
    }

    val safeClickListener = SafeClickListener {
        if (validations()) {
            uploadImages(base64String, {
                val tenantList = TenantList(
                    Active = "1",
                    MobileNo = activityTenantsBinding.editNumber.text.toString(),
                    Name = activityTenantsBinding.editName.text.toString(),
                    apartmentId = apartmentSelected?.ID.toString(),
                    floorno = selectFloor,
                    roomno = roomSelected?.ID.toString(),
                    flatno = FlatSelected?.ID.toString(),
                    Gender = activityTenantsBinding.genderSpinner.selectedItem.toString(),
                    Profession = "",
                    rent = activityTenantsBinding.editRentPerDay.text.toString(),
                    rentstatus = "",
                    duedate = "",
                    paymentmode = "",
                    securitydeposit = "",
                    joinedon = currentdate(),
                    mailid = "",
                    ProofImageF = "",
                    ProofImageB = "",
                    CreatedBy = prefmanager.userData?.UserName,
                    UpdatedOn = currentdate(),
                    checkin = activityTenantsBinding.editCheckIn.text.toString()
                        .dateFormat("dd-MM-yyyy", "yyyy-MM-dd"),
                    checkout = activityTenantsBinding.editCheckOut.text.toString()
                        .dateFormat("dd-MM-yyyy", "yyyy-MM-dd"),
                    paid = "0",
                    total = "0",
                    countrycode = activityTenantsBinding.textDropDownChooseCountry.text.toString(),
                    details = "${roomSelected?.roomname} - B${bedSelected?.number}",
                    userImage = imagePath
                )
                addApartmentViewModel?.addTenant(tenantList, success = {
                    updatebeds(
                        roomSelected,
                        bedSelected,
                        activityTenantsBinding.editNumber.text.toString()
                    )
                }, error = {
                    showToast(this, it)
                })
            })
        }
    }

    private fun validations(): Boolean {
        var flag = true
        if (activityTenantsBinding.editName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
        } else if (selectApart?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Apartment", Toast.LENGTH_SHORT).show()
        } else if (selectFloor?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Floor", Toast.LENGTH_SHORT).show()
        } else if (selectFlat?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Flat", Toast.LENGTH_SHORT).show()
        } else if (selectRoom?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Select Room", Toast.LENGTH_SHORT).show()
        } else if (selectBed?.isBlank() == true) {
            flag = false
            Toast.makeText(this, "Allocate Bed", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckIn.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckIn Date", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editCheckOut.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select CheckOut Date", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editRentPerDay.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Rent per day", Toast.LENGTH_SHORT).show()
        } else if (base64String?.isEmpty() == true) {
            flag = false
            Toast.makeText(this, "Capture User Image", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    private fun getApartments() {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList.clear()
                    it.apartmentList.add(
                        0,
                        ApartmentList(ID = -1, apartmentname = "Select Apartment")
                    )
                    apartmentList.addAll(it.apartmentList)
                    activityTenantsBinding.apartmentSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, apartmentList!!
                    )
                    activityTenantsBinding.apartmentSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectApart = if (selectedItem != "Select Apartment") {
                                    apartmentSelected = apartmentList[p2]
                                    getFloors(apartmentSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = ""
        )
    }

    private fun getFloors(apartmentSelected: ApartmentList) {
        if (apartmentSelected?.nooffloors != null) {
            if (!apartmentSelected?.nooffloors.isNullOrEmpty()) {
                val list: ArrayList<String> = Gson().fromJson(apartmentSelected?.nooffloors, type)
                list.add(0, "Select Floor")
                floorList.clear()
                floorList.addAll(list)
                activityTenantsBinding.floorSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, floorList!!
                )
                activityTenantsBinding.floorSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            val selectedItem = p0?.getItemAtPosition(p2).toString()
                            selectFloor = if (selectedItem != "Select Floor") {
                                getFlats(selectedItem!!)
                                selectedItem
                            } else ""
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }
        }
    }

    private fun getFlats(selectFloor: String) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatList.clear()
                    it.flatList.add(
                        0,
                        FlatData.FlatList(ID = -1, flatname = "Select Flat")
                    )
                    flatList.addAll(it.flatList)
                    activityTenantsBinding.flatSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, flatList!!
                    )
                    activityTenantsBinding.flatSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectFlat = if (selectedItem != "Select Flat") {
                                    FlatSelected = flatList[p2]
                                    getRooms(FlatSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {

                }
            },
            error = {
                showToast(this, it)
            },
            prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentSelected?.ID.toString(),
            selectFloor
        )
    }

    private fun getRooms(flatSelected: FlatData.FlatList) {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomList.clear()
                    it.roomsList.add(
                        0,
                        RoomData.RoomsList(ID = -1, roomname = "Select Room")
                    )
                    roomList.addAll(it.roomsList)
                    activityTenantsBinding.roomSpinner.adapter = ArrayAdapter(
                        this,
                        R.layout.spinner_item, roomList!!
                    )
                    activityTenantsBinding.roomSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                val selectedItem = p0?.getItemAtPosition(p2).toString()
                                selectRoom = if (selectedItem != "Select Room") {
                                    roomSelected = roomList[p2]
                                    showAvailableBeds(roomSelected!!)
                                    selectedItem
                                } else ""
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {}
                        }
                } else {

                }
            },
            error = {
                showToast(this, it)
            },
            apartmentid = apartmentSelected?.ID.toString(),
            floorno = selectFloor!!,
            flatno = flatSelected.ID.toString()
        )
    }

    private fun showAvailableBeds(roomSelected: RoomData.RoomsList) {
        if (roomSelected.available != null) {
            if (!roomSelected?.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomSelected?.available, bedsType)
                bedsList.clear()
                list.forEach { bed ->
                    if (bed.userId.isNullOrEmpty()) {
                        bedsList.add(bed)
                    }
                }
                bedsList.add(0, Beds(number = "Available Beds"))
                activityTenantsBinding.bedsSpinner.adapter = ArrayAdapter(
                    this,
                    R.layout.spinner_item, bedsList!!
                )
                activityTenantsBinding.bedsSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            val selectedItem = p0?.getItemAtPosition(p2).toString()
                            selectBed = if (selectedItem != "Available Beds") {
                                bedSelected = bedsList[p2]
//                                updatebeds(
//                                    roomSelected,
//                                    bedSelected!!,
//                                    activityTenantsBinding.editNumber.text.toString()
//                                )
                                selectedItem
                            } else ""
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }
        }
    }


    private fun updatebeds(
        roomSelected: RoomData.RoomsList?,
        bedSelected: Beds?,
        mobNumber: String
    ) {
        if (roomSelected?.available != null) {
            if (!roomSelected.available.isNullOrEmpty()) {
                val list: ArrayList<Beds> = Gson().fromJson(roomSelected?.available, bedsType)
                list.forEach { bed ->
                    if (bed.number == bedSelected?.number) {
                        bed.userId = mobNumber
                        bed.occupied = true
                    }
                }
                roomSelected.available = Gson().toJson(list)
                roomSelected.createdby = prefmanager.userData?.UserName
                roomSelected.updatedon = currentdate()
                roomSelected.update = "update"
                Log.v("SELECTED_ROOM", Gson().toJson(roomSelected))
                addApartmentViewModel?.addRooms(roomSelected, success = {
                    showToast(this, "Tenant Added Successfully")
                    onBackPressed()
                }, error = {
                    showToast(this, it)
                })
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    private fun selectCheckInDate(checkIn: EditText) {
        var calendar = Calendar.getInstance()
        val datePickerDialog = this.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    checkIn.setText("$dayOfMonth-${monthOfYear.plus(1)}-$year")

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        // datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()
    }

    private fun selectcheckOutDate(checkOut: EditText) {
        var calendar = Calendar.getInstance()
        val datePickerDialog = this.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    checkOut.setText("$dayOfMonth-${monthOfYear.plus(1)}-$year")

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        //datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()
    }

    private fun captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoUri = createImageUri()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraCaptureLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                requestCodeCameraPermission
            )
        }
    }

    private val cameraCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                base64String = bitmapToBase64(bitmap)
                activityTenantsBinding.userImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun pickImageFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryPickerLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                requestCodeStoragePermission
            )
        }
    }

    // Register for gallery picker result
    private val galleryPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                try {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                    base64String = bitmapToBase64(bitmap)
                    activityTenantsBinding.userImage.setImageBitmap(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Helper function to create a file URI for camera image
    private fun createImageUri(): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir: File = getExternalFilesDir(null) ?: return null
            val imageFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            FileProvider.getUriForFile(
                this,
                "com.shaikhomes.anyrent.provider",
                imageFile
            )
        } catch (ex: IOException) {
            Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            null
        }
    }

    // Function to convert Bitmap to Base64 String
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestCodeCameraPermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            requestCodeStoragePermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImages(base64Image: String?, success: () -> Unit) {
        val imageData = ImageData(image = base64Image)
        RetrofitInstance.api.postImage(imageData)
            .enqueue(object : Callback<ResponseData> {
                override fun onResponse(
                    call: Call<ResponseData>,
                    response: Response<ResponseData>
                ) {
                    if (response.body()?.status == "200") {
                        response.body()?.message?.substringAfterLast("\\")
                            ?.let { imagePath = "https://anyrent.shaikhomes.com/ImageStorage/$it" }
                        success.invoke()
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {

                }
            })
    }
}