package com.shaikhomes.anyrent

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.kevinschildhorn.otpview.OTPView
import com.shaikhomes.anyrent.databinding.ActivityAddExpensesBinding
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.customviews.SafeClickListener
import com.shaikhomes.anyrent.ui.models.ExpensesList
import com.shaikhomes.anyrent.ui.models.ImageData
import com.shaikhomes.anyrent.ui.models.ResponseData
import com.shaikhomes.anyrent.ui.network.RetrofitInstance
import com.shaikhomes.anyrent.ui.utils.FileUtil
import com.shaikhomes.anyrent.ui.utils.ImagePicker
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.dateFormat
import com.shaikhomes.anyrent.ui.utils.showToast
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

class AddExpenses : AppCompatActivity() {

    private lateinit var activityAddExpensesBinding: ActivityAddExpensesBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    var file: File? = null
    var imagePath = ""
    var paymentMode = ""
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
        activityAddExpensesBinding.apply {
            apartmentName.apply {
                text = prefmanager.selectedApartment?.apartmentname
            }
            apartmentAddress.apply {
                text = prefmanager.selectedApartment?.address
            }
            editPaidOn.setOnClickListener {
                selectDate(editPaidOn)
            }
            editDebitAmount?.doAfterTextChanged {
                if (editDebitAmount.text.toString().isNullOrEmpty()) {
                    editDebitAmount.setText("0")
                }
            }
            editCreditAmount?.doAfterTextChanged {
                if (editCreditAmount.text.toString().isNullOrEmpty()) {
                    editCreditAmount.setText("0")
                }
            }
            onlineToggle.isChecked = true
            onlineToggle.setTextColor(Color.parseColor("#FFFFFF"))
            paymentMode = "Online"
            onlineToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    paymentMode = "Online"
                    cashToggle.isChecked = false
                    onlineToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    cashToggle.setTextColor(Color.parseColor("#000000"))
                }
            }

            cashToggle.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    paymentMode = "Cash"
                    onlineToggle.isChecked = false
                    cashToggle.setTextColor(Color.parseColor("#FFFFFF"))
                    onlineToggle.setTextColor(Color.parseColor("#000000"))
                }
            }
            addExpenses.setOnClickListener(safeClickListener)
        }
    }

    val safeClickListener = SafeClickListener {
        if (validations()) {
            if (!activityAddExpensesBinding.editCreditAmount.text.toString()
                    .isNullOrEmpty() && activityAddExpensesBinding.editCreditAmount.text.toString() != "0"
            ) {
                val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
                val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
                AlertDialog.Builder(this@AddExpenses).apply {
                    this.setMessage("Please enter OTP to add credit amount")
                    this.setView(dialogView)
                    this.setPositiveButton(
                        "YES"
                    ) { p0, p1 ->
                        if (otpView.getStringFromFields() == "278692") {
                            sendData()
                        } else showToast(this@AddExpenses, "Incorrect OTP")
                    }
                    this.setNegativeButton(
                        "NO"
                    ) { p0, p1 ->
                        p0.dismiss()
                    }
                    this.setCancelable(true)
                    this.show()
                }
            } else sendData()

        }

    }

    private fun sendData() {
        uploadImages(base64String, {
            val expensesList = ExpensesList(
                userid = prefmanager?.userData?.UserId.toString(),
                apartmentid = prefmanager.selectedApartment?.ID.toString(),
                creditAmount = if (activityAddExpensesBinding.editCreditAmount?.text.toString()
                        .isNullOrEmpty()
                ) "0" else activityAddExpensesBinding.editCreditAmount?.text.toString(),
                category = "",
                debitAmount = if (activityAddExpensesBinding.editDebitAmount?.text.toString()
                        .isNullOrEmpty()
                ) "0" else activityAddExpensesBinding.editDebitAmount?.text.toString(),
                receivedOn = activityAddExpensesBinding.editPaidOn?.text.toString()
                    ?.dateFormat("dd-MM-yyyy", "yyyy-MM-dd"),
                paymentMode = paymentMode,
                txnId = activityAddExpensesBinding.editTxnId?.text.toString(),
                notes = activityAddExpensesBinding.editDesc.text.toString(),
                picture = imagePath,
                receivedBy = "",
                updatedon = currentdate()
            )
            addApartmentViewModel?.addExpenses(expensesList, success = {
                showToast(this, "Expense Captured Successfully")
                onBackPressed()
            }, error = {
                showToast(this, it)
            })

        })
    }

    private fun validations(): Boolean {
        var flag = true
        if (activityAddExpensesBinding.editDesc.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Description", Toast.LENGTH_SHORT).show()
        } else if (activityAddExpensesBinding.editDebitAmount.text.toString()
                .isEmpty() && activityAddExpensesBinding.editCreditAmount.text.toString().isEmpty()
        ) {
            flag = false
            Toast.makeText(this, "Enter Credit/Debit Amount", Toast.LENGTH_SHORT).show()
        } else if (activityAddExpensesBinding.editTxnId.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Enter Transaction Id", Toast.LENGTH_SHORT).show()
        } else if (activityAddExpensesBinding.editPaidOn.text.toString().isEmpty()) {
            flag = false
            Toast.makeText(this, "Select Paid On Date", Toast.LENGTH_SHORT).show()
        } else if (base64String?.isEmpty() == true) {
            flag = false
            Toast.makeText(this, "Capture User Image", Toast.LENGTH_SHORT).show()
        }
        return flag
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
    }

    private fun selectDate(checkIn: EditText) {
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