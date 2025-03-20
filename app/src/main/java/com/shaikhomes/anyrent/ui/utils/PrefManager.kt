package com.shaikhomes.anyrent.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.shaikhomes.anyrent.ui.models.ApartmentList
import com.shaikhomes.anyrent.ui.models.UserDetailsList

class PrefManager(private val context: Context) {
    /**
     * Creates Shared Preference Editor object for editing preference values
     *
     **/
    private val editor: SharedPreferences.Editor
        @SuppressLint("CommitPrefEdits") get() = preference.edit()

    /**
     * Create shared preference
     *
     **/
    private val preference: SharedPreferences
        get() = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)


    var userData: UserDetailsList?
        get() {
            val data = preference.getString(USER_DATA, null)
            return if (data == null) null
            else try {
                Gson().fromJson(data, UserDetailsList::class.java)
            } catch (exception: IllegalStateException) {
                null
            } catch (exception: JsonSyntaxException) {
                null
            }
        }
        set(value) {
            try {
                val data = Gson().toJson(value)
                editor.putString(USER_DATA, data).apply()
            } catch (exception: IllegalStateException) {
                exception.printStackTrace()
            } catch (exception: JsonSyntaxException) {
                exception.printStackTrace()
            }
        }

    var selectedApartment: ApartmentList?
        get() {
            val data = preference.getString(SELECTED_APARTMENT, null)
            return if (data == null) null
            else try {
                Gson().fromJson(data, ApartmentList::class.java)
            } catch (exception: IllegalStateException) {
                null
            } catch (exception: JsonSyntaxException) {
                null
            }
        }
        set(value) {
            try {
                val data = Gson().toJson(value)
                editor.putString(SELECTED_APARTMENT, data).apply()
            } catch (exception: IllegalStateException) {
                exception.printStackTrace()
            } catch (exception: JsonSyntaxException) {
                exception.printStackTrace()
            }
        }

    var isLoggedIn: Boolean
        get() = preference.getBoolean(IS_LOGGED_IN, false)
        set(value) {
            editor.putBoolean(IS_LOGGED_IN, value).apply()
        }

    var listPos: Int
        get() = preference.getInt(LIST_POS, 0)
        set(value) {
            editor.putInt(LIST_POS, value).apply()
        }
}