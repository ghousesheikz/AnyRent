package com.shaikhomes.smartdiary

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.shaikhomes.anyrent.databinding.ActivityAddExpensesBinding
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.models.ImageData
import com.shaikhomes.smartdiary.ui.models.ResponseData
import com.shaikhomes.smartdiary.ui.network.RetrofitInstance
import com.shaikhomes.smartdiary.ui.utils.FileUtil
import com.shaikhomes.smartdiary.ui.utils.ImagePicker
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenses : AppCompatActivity() {

    private lateinit var activityAddExpensesBinding: ActivityAddExpensesBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    var file: File? = null
    var imagePath = ""
    private var base64String: String? = ""
    private val requestCodeCameraPermission = 101
    private val requestCodeStoragePermission = 102
    private var photoUri: Uri? = null
    private var addApartmentViewModel: AddApartmentViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAddExpensesBinding = ActivityAddExpensesBinding.inflate(layoutInflater)
        setContentView(activityAddExpensesBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Add Expenses"
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        activityAddExpensesBinding?.txnImage?.setOnClickListener {
            selectImage()
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
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
                    activityAddExpensesBinding.txnImage?.setImageBitmap(
                        BitmapFactory.decodeFile(
                            file?.absolutePath
                        )
                    )
                }
            }

        }
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
                activityAddExpensesBinding.txnImage.setImageBitmap(bitmap)
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
                    activityAddExpensesBinding.txnImage.setImageBitmap(bitmap)
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