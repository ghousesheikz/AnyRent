package com.shaikhomes.smartdiary.ui.customviews

import android.os.SystemClock
import android.view.View

class SafeClickListener(
    private var defaultInterval: Long = 1000L,
    private val listener: (View?) -> Unit
) :
    View.OnClickListener {

    private var lastTimeClicked: Long = 0

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        listener.invoke(v)
    }
}