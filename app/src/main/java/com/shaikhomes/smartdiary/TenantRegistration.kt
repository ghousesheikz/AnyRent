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
import android.text.Html
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
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
import com.shaikhomes.anyrent.databinding.ActivityTenantRegistrationBinding
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

class TenantRegistration : AppCompatActivity() {
    private lateinit var activityTenantsBinding: ActivityTenantRegistrationBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var base64String: String? = ""
    private val requestCodeCameraPermission = 101
    private val requestCodeStoragePermission = 102
    private var photoUri: Uri? = null
    val type = object : TypeToken<ArrayList<String>>() {}.type
    val bedsType = object : TypeToken<ArrayList<Beds>>() {}.type
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var roomsList: RoomData.RoomsList? = null
    private var selectedBed: Beds? = null
    private var apartmentList: ApartmentList? = null
    private var flatList: FlatData.FlatList? = null
    private var genderist = arrayListOf<String>()
    var file: File? = null
    var imagePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantsBinding = ActivityTenantRegistrationBinding.inflate(layoutInflater)
        setContentView(activityTenantsBinding.root)
        supportActionBar?.title = "Tenants"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        roomsList =
            Gson().fromJson(intent.getStringExtra("ROOM_SELECT"), RoomData.RoomsList::class.java)
        selectedBed = Gson().fromJson(intent.getStringExtra("BED_SELECT"), Beds::class.java)
        getApartment(roomsList?.apartmentid)
        getFlat(roomsList?.apartmentid, roomsList?.floorno)
        activityTenantsBinding.apply {
            floor.setText(
                Html.fromHtml(
                    "Floor : <font color='#000E77'>${
                        roomsList?.floorno
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
            room.setText(
                Html.fromHtml(
                    "Room : <font color='#000E77'>${
                        roomsList?.roomname
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
            bed.setText(
                Html.fromHtml(
                    "Bed No : <font color='#000E77'>${
                        selectedBed?.number
                    }</font>"
                ),
                TextView.BufferType.SPANNABLE
            )
        }
        genderist.add("male")
        genderist.add("female")
        activityTenantsBinding.genderSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item, genderist!!
        )
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
        activityTenantsBinding.editCheckIn.setOnClickListener {
            selectCheckInDate(activityTenantsBinding.editCheckIn)
        }
        activityTenantsBinding.editJoiningDate.setOnClickListener {
            selectCheckInDate(activityTenantsBinding.editJoiningDate)
        }
        activityTenantsBinding.editCheckOut.setOnClickListener {
            selectcheckOutDate(activityTenantsBinding.editCheckOut)
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
                    apartmentId = roomsList?.apartmentid.toString(),
                    floorno = roomsList?.floorno,
                    roomno = roomsList?.ID?.toString(),
                    flatno = roomsList?.flatno.toString(),
                    Gender = activityTenantsBinding.genderSpinner.selectedItem.toString(),
                    Profession = "",
                    rent = activityTenantsBinding.editRentPerDay?.text.toString(),
                    rentstatus = "",
                    duedate = "",
                    paymentmode = "",
                    securitydeposit = activityTenantsBinding.editSecurityDeposit.text.toString().trim(),
                    joinedon = activityTenantsBinding.editJoiningDate.text.toString()
                        .dateFormat("dd-MM-yyyy", "yyyy-MM-dd"),
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
                    details = "${roomsList?.roomname} - B${selectedBed?.number}",
                    userImage = imagePath
                )
                addApartmentViewModel?.addTenant(tenantList, success = {
                    updatebeds(
                        roomsList,
                        selectedBed,
                        activityTenantsBinding.editNumber.text.toString()
                    )
                }, error = {
                    showToast(this, it)
                })
            })

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

    private fun validations(): Boolean {
        var flag = true
        if (activityTenantsBinding.editName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (activityTenantsBinding.editNumber.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
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
        }  else if (activityTenantsBinding.editJoiningDate.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Joining Date", Toast.LENGTH_SHORT).show()
        }  else if (activityTenantsBinding.editSecurityDeposit.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Security Deposit", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    private fun getApartment(apartmentid: String?) {
        addApartmentViewModel?.getApartments(
            success = {
                if (it.apartmentList.isNotEmpty()) {
                    apartmentList = it.apartmentList.first()
                    activityTenantsBinding.apply {
                        apartment.setText(
                            Html.fromHtml(
                                "Apartment : <font color='#000E77'>${
                                    apartmentList?.apartmentname
                                }</font>"
                            ),
                            TextView.BufferType.SPANNABLE
                        )
                    }
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentid!!
        )
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

    private fun getFlat(apartmentid: String?, floorNo: String?) {
        addApartmentViewModel?.getFlats(
            success = {
                if (it.flatList.isNotEmpty()) {
                    flatList = it.flatList.first()
                    activityTenantsBinding.apply {
                        flat.setText(
                            Html.fromHtml(
                                "Flat : <font color='#000E77'>${
                                    flatList?.flatname
                                }</font>"
                            ),
                            TextView.BufferType.SPANNABLE
                        )
                    }
                }
            },
            error = {
                showToast(this, it)
            },
            userid = prefmanager.userData?.UserId.toString(),
            apartmentid = apartmentid!!, floorno = floorNo!!
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
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