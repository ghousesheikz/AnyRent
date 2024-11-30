package com.shaikhomes.smartdiary.ui.utils

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shaikhomes.smartdiary.ui.models.CountryCode
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Random

fun currentdate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(Date())
}

fun currentonlydate(pattern: String = "yyyy-MM-dd"): String {
    val sdf = SimpleDateFormat(pattern)
    return sdf.format(Date())
}

fun formatDate(format: String): String {
    val sdf = SimpleDateFormat(format)
    return sdf.format(Date())
}

fun String.dateFormat(fromFormat: String, toFormat: String): String {
    val parser = SimpleDateFormat(fromFormat)
    val formatter = SimpleDateFormat(toFormat)
    return formatter.format(parser.parse(this))
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT)
        .show()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun isAccessibilityOn(
    context: Context,
    clazz: Class<out AccessibilityService?>
): Boolean {
    var accessibilityEnabled = 0
    val service: String = context.packageName + "/" + clazz.canonicalName
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            context.applicationContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
    } catch (ignored: Settings.SettingNotFoundException) {
    }
    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    if (accessibilityEnabled == 1) {
        val settingValue: String = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        colonSplitter.setString(settingValue)
        while (colonSplitter.hasNext()) {
            val accessibilityService = colonSplitter.next()
            if (accessibilityService.equals(service, ignoreCase = true)) {
                return true
            }
        }
    }
    return false
}

fun randomNumber(): Int {
    val r = Random(System.currentTimeMillis())
    return (1 + r.nextInt(2)) * 10000 + r.nextInt(10000)
}

fun getCountryList(context: Context): List<CountryCode> {
    val jsonString: String
    try {
        jsonString = context.assets.open("country_list.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        return arrayListOf()
    }
    val listType = object : TypeToken<List<CountryCode>>() {}.type
    return Gson().fromJson(jsonString, listType)
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDaysBetween(
    startDateStr: String,
    endDateStr: String,
    pattern: String = "dd-MM-yyyy"
): Long {
    // Define the date formatter
    val formatter = DateTimeFormatter.ofPattern(pattern)
    // Parse the start and end dates
    val startDate = LocalDate.parse(startDateStr, formatter)
    val endDate = LocalDate.parse(endDateStr, formatter)
    // Calculate the difference in days
    return ChronoUnit.DAYS.between(startDate, endDate)
}