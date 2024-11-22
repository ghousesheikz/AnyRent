package com.shaikhomes.smartdiary

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.shaikhomes.anyrent.R
import com.shaikhomes.smartdiary.ui.PropertyActivity
import com.shaikhomes.smartdiary.ui.utils.PrefManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    val getNumbers = arrayListOf<String>("8688589282", "9182488911", "9949192977", "9652715115")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        if (!isAccessibilityOn(this@SplashActivity, WhatsappAccessibilityService::class.java)) {
//            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            this.startActivity(intent)
//        } else {
//Uri imgUri = Uri.parse(pictureFile.getAbsolutePath());
//    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
//    whatsappIntent.setType("text/plain");
//    whatsappIntent.setPackage("com.whatsapp");
//    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "The text you wanted to share");
//    whatsappIntent.putExtra(Intent.EXTRA_STREAM, imgUri);
//    whatsappIntent.setType("image/jpeg");
//    whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//    try {
//        activity.startActivity(whatsappIntent);
//    } catch (android.content.ActivityNotFoundException ex) {
//        ToastHelper.MakeShortText("Whatsapp have not been installed.");
//    }
//        }
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            PrefManager(this).apply {
                if (this.isLoggedIn) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }, 2000)
    }

    override fun onResume() {
        super.onResume()
    }


}