package com.shaikhomes.smartdiary

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityRoomsAvailableBinding
import com.shaikhomes.smartdiary.ui.adapters.RoomsAdapter
import com.shaikhomes.smartdiary.ui.apartment.AddApartmentViewModel
import com.shaikhomes.smartdiary.ui.utils.PrefManager
import com.shaikhomes.smartdiary.ui.utils.showToast
import java.io.File
import java.io.FileOutputStream

class RoomsAvailableActivity : AppCompatActivity() {
    private lateinit var activityRoomsBinding: ActivityRoomsAvailableBinding
    private val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var addApartmentViewModel: AddApartmentViewModel? = null
    private var roomAdapter: RoomsAdapter? = null
    val type = object : TypeToken<ArrayList<String>>() {}.type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRoomsBinding = ActivityRoomsAvailableBinding.inflate(layoutInflater)
        setContentView(activityRoomsBinding.root)
        // Change toolbar title
        supportActionBar?.title = "Room Details"
        // Enable the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        addApartmentViewModel =
            ViewModelProvider(this).get(AddApartmentViewModel::class.java)
        activityRoomsBinding.apply {
            roomAvailableList.layoutManager =
                LinearLayoutManager(this@RoomsAvailableActivity)
            roomAdapter =
                RoomsAdapter(this@RoomsAvailableActivity, arrayListOf(), true, true).apply {
                    setBedClickListener { roomsList, beds ->
                        val intent =
                            Intent(this@RoomsAvailableActivity, TenantRegistration::class.java)
                        intent.putExtra("ROOM_SELECT", Gson().toJson(roomsList))
                        intent.putExtra("BED_SELECT", Gson().toJson(beds))
                        startActivity(intent)
                        finish()
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
                                this@RoomsAvailableActivity,
                                "Failed to generate QR Code",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    setTenantClickListener { roomsList, beds ->
                        addApartmentViewModel?.getTenants(success = { tenantList ->
                            if (tenantList.tenant_list.isNotEmpty()) {
                                val intent =
                                    Intent(this@RoomsAvailableActivity, TenantOverview::class.java)
                                intent.putExtra(
                                    "tenant",
                                    Gson().toJson(tenantList.tenant_list.first())
                                )
                                startActivity(intent)
                            }
                        }, error = {
                            showToast(this@RoomsAvailableActivity, it)
                        }, beds.userId ?: "", "", "", "")
                    }
                }
            roomAvailableList.adapter = roomAdapter
        }
        getRooms()
    }

    private fun getRooms() {
        addApartmentViewModel?.getRooms(
            success = {
                if (it.roomsList.isNotEmpty()) {
                    roomAdapter?.updateList(it.roomsList.sortedWith(compareBy {
                        extractNumber(
                            it.roomname ?: ""
                        )
                    }))
                } else roomAdapter?.updateList(arrayListOf())
            },
            error = {
                showToast(this, it)
            },
            apartmentid = prefmanager.selectedApartment?.ID.toString(),
            floorno = "",
            flatno = ""
        )
    }

    fun extractNumber(input: String): Int {
        val regex = Regex("\\d+")
        val match = regex.find(input)
        return match?.value?.toInt()
            ?: Int.MAX_VALUE // If no number, treat it as the largest possible value
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back
        return true
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
            shareQRCode(bitmap, url)
        }

        dialog.show()
    }

    private fun shareQRCode(bitmap: Bitmap?, url: String) {
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
        return androidx.core.content.FileProvider.getUriForFile(
            this,
            "com.shaikhomes.anyrent.provider",
            file
        )
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