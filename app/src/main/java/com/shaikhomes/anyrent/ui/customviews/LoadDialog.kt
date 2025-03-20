package com.shaikhomes.anyrent.ui.customviews

import android.content.Context
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import com.shaikhomes.anyrent.R

class LoadDialog(context: Context, @LayoutRes var layoutRes: Int = R.layout.dialog_load) :
    AlertDialog(context, R.style.LoadDialogTheme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        setCancelable(false)
    }


}


/**
 * to show progress dialog
 *
 * @author Ratnesh Kumar Ratan
 * @since 17/03/2020
 **/
fun LoadDialog.showProgress() {
    if (!isShowing) {
        show()
    }
}

/**
 * To hide progress dialog
 *
 * @author Ratnesh Kumar Ratan
 * @since 17/03/2020
 **/
fun LoadDialog.dismissProgress() {
    if (isShowing)
        dismiss()
}

