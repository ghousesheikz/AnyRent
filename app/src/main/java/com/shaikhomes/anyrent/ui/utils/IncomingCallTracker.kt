package com.shaikhomes.anyrent.ui.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class IncomingCallTracker : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        try {
            val bundle = p1?.extras;
            val keys = bundle?.keySet()
            for (key in keys!!) {
                Log.i("MYAPP##", key + "=" + bundle.getString(key));
            }
        }catch (exp:Exception){
            exp.printStackTrace()
        }
    }
}