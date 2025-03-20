package com.shaikhomes.anyrent.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.kevinschildhorn.otpview.OTPView
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityPropertyBinding
import com.shaikhomes.anyrent.ApartmentActivity
import com.shaikhomes.anyrent.LoginActivity
import com.shaikhomes.anyrent.ui.adapters.ApartmentAdapter
import com.shaikhomes.anyrent.ui.apartment.AddApartmentViewModel
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.Beds
import com.shaikhomes.anyrent.ui.models.RoomData
import com.shaikhomes.anyrent.ui.utils.PrefManager
import com.shaikhomes.anyrent.ui.utils.currentdate
import com.shaikhomes.anyrent.ui.utils.hideKeyboard
import com.shaikhomes.anyrent.ui.utils.showToast
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
                val intent = Intent(this@PropertyActivity, ApartmentActivity::class.java)
                intent.putExtra("apartment", Gson().toJson(it))
                startActivity(intent)
            }
            setPropertyClickListener {
                prefmanager.selectedApartment = it
                onBackPressed()
            }
            setDeleteClickListener { apartment ->
                deleteApartment(apartment)
            }
            setQRCodeClick {apartment ->
                val url = "https://myhotelsbooking.com/TenantRegistrationForm/?apartment=${apartment.ID}&userid=${prefmanager?.userData?.UserId}"
                // Generate QR Code
                val bitmap = generateQRCode(url)
                if (bitmap != null) {
                    // Show QR code in a popup dialog
                    showQRCodeDialog(bitmap,url)
                } else {
                    Toast.makeText(this@PropertyActivity, "Failed to generate QR Code", Toast.LENGTH_SHORT).show()
                }
            }
        }
        activityPropertyBinding.propertyList.apply {
            layoutManager = LinearLayoutManager(this@PropertyActivity)
            adapter = apartmentAdapter
        }
        getApartments()
    }


    private fun showQRCodeDialog(bitmap: Bitmap,url: String) {
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

    private fun shareQRCode(bitmap: Bitmap?,url:String) {
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

    private fun deleteApartment(apartmentList: ApartmentList) {
        val dialogView = layoutInflater.inflate(R.layout.otp_view, null)
        val otpView = dialogView.findViewById<OTPView>(R.id.otpView)
        AlertDialog.Builder(this).apply {
            this.setMessage("Do you want to delete ${apartmentList.apartmentname}?")
            this.setView(dialogView)
            this.setPositiveButton(
                "YES"
            ) { p0, p1 ->
                if (otpView.getStringFromFields() == "009289") {
                    apartmentList.update = "delete"
                    addApartmentViewModel?.addApartment(apartmentList, success = {
                        showToast(this@PropertyActivity, "Deleted Successfully")
                        getApartments()
                    }, error = {
                        showToast(this@PropertyActivity, it)
                    })
                } else showToast(this@PropertyActivity, "Incorrect OTP")
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