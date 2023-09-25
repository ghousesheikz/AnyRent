package com.shaikhomes.smartdiary.ui.utils

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date

fun currentdate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
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
