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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityEditTenantBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.customviews.SafeClickListener
import com.shaikhomes.smartdiary.ui.models.ImageData
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.models.TenantList
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.FileUtil
import com.shaikhomes.smartdiary.ui.utils.ImagePicker
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.currentdate
import com.shaikhomes.smartdiary.ui.utils.dateFormat
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

class EditTenant : AppCompatActivity() {
    private lateinit var activityTenantOverviewBinding: ActivityEditTenantBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var tenantList: TenantList? = null
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var genderist = arrayListOf<String>()
    var file: File? = null
    var imagePath = ""
    private var base64String: String? = ""
    private val requestCodeCameraPermission = 101
    private var photoUri: Uri? = null
    private val requestCodeStoragePermission = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityTenantOverviewBinding = ActivityEditTenantBinding.inflate(layoutInflater)
        setContentView(activityTenantOverviewBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Tenant Edit"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        tenantList =
            Gson().fromJson(intent.getStringExtra("edit_tenant"), TenantList::class.java)
        activityTenantOverviewBinding.apply {
            if (!tenantList?.userImage.isNullOrEmpty()) {
                circularImageView.visibility = View.VISIBLE
                profileImage.visibility = View.GONE
                Glide.with(this@EditTenant)
                    .load(tenantList?.userImage)
                    .transform(CircleTransformation()) // Apply custom circle transformation
                    .into(circularImageView)
            } else {
                circularImageView.visibility = View.GONE
                profileImage.visibility = View.VISIBLE
                profileImage.text = tenantList?.Name?.first().toString()
            }
            editName.setText(tenantList?.Name)
            editNumber.setText(tenantList?.MobileNo)
            genderist.add("male")
            genderist.add("female")
            genderSpinner.adapter = ArrayAdapter(
                this@EditTenant,
                R.layout.spinner_item, genderist!!
            )
            if (tenantList?.Gender == "male") {
                genderSpinner.setSelection(0)
            } else if (tenantList?.Gender == "female") {
                genderSpinner.setSelection(1)
            }
            editJoiningDate.setOnClickListener {
                selectJoiningDate(editJoiningDate)
            }
            editRentPerDay.setText(tenantList?.rent)
            editJoiningDate.setText(
                tenantList?.joinedon?.dateFormat(
                    "dd-MM-yyyy hh:mm:ss",
                    "dd-MM-yyyy"
                )
            )
            editSecurityDeposit.setText(tenantList?.securitydeposit)
            userImage?.setOnClickListener {
                selectImage()
            }
            addTenant.setOnClickListener(safeClickListener)
        }

    }

    val safeClickListener = SafeClickListener {
        if (validations()) {
            uploadImages(base64String, {
                tenantList?.Name = activityTenantOverviewBinding.editName.text.toString()
                tenantList?.Gender =
                    activityTenantOverviewBinding.genderSpinner.selectedItem.toString()
                tenantList?.rent = activityTenantOverviewBinding.editRentPerDay?.text.toString()
                tenantList?.securitydeposit =
                    activityTenantOverviewBinding.editSecurityDeposit.text.toString().trim()
                tenantList?.joinedon = activityTenantOverviewBinding.editJoiningDate.text.toString()
                    .dateFormat("dd-MM-yyyy", "yyyy-MM-dd")
                tenantList?.CreatedBy = prefmanager.userData?.UserName
                tenantList?.UpdatedOn = currentdate()
                if (!base64String.isNullOrEmpty()) {
                    tenantList?.userImage = imagePath
                }
                tenantList?.checkin =
                    tenantList?.checkin?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.checkout =
                    tenantList?.checkout?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.UpdatedOn = currentdate()
                tenantList?.duedate =
                    tenantList?.duedate?.dateFormat("dd-MM-yyyy hh:mm:ss", "yyyy-MM-dd")
                tenantList?.update = "update"
                addApartmentViewModel?.addTenant(tenantList!!, success = {
                    showToast(this, "Tenant Updated Successfully")
                    onBackPressed()
                }, error = {
                    showToast(this, it)
                })
            })
        }
    }

    private fun uploadImages(base64Image: String?, success: () -> Unit) {
        if (base64Image.isNullOrEmpty()) {
            success.invoke()
            return
        }
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

    private fun validations(): Boolean {
        var flag = true
        if (activityTenantOverviewBinding.editName.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show()
        } else if (activityTenantOverviewBinding.editRentPerDay.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Rent per day", Toast.LENGTH_SHORT).show()
        } else if (activityTenantOverviewBinding.editJoiningDate.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Joining Date", Toast.LENGTH_SHORT).show()
        } else if (activityTenantOverviewBinding.editSecurityDeposit.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Security Deposit", Toast.LENGTH_SHORT).show()
        }
        return flag
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
                    activityTenantOverviewBinding?.userImage?.setImageBitmap(
                        BitmapFactory.decodeFile(
                            file?.absolutePath
                        )
                    )
                }
            }

        }
    }

    private fun selectJoiningDate(checkIn: EditText) {
        var calendar = Calendar.getInstance()
        val datePickerDialog = this.let { it1 ->
            DatePickerDialog(
                it1,
                { _, year, monthOfYear, dayOfMonth ->
                    var checkinDate = "$dayOfMonth-${monthOfYear.plus(1)}-$year"
                    checkinDate = checkinDate.dateFormat("dd-MM-yyyy", "dd-MM-yyyy")
                    checkIn.setText(checkinDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        // datePickerDialog?.datePicker?.minDate = calendar.timeInMillis
        datePickerDialog?.show()
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
                activityTenantOverviewBinding.userImage.setImageBitmap(bitmap)
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
                    activityTenantOverviewBinding.userImage.setImageBitmap(bitmap)
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
}