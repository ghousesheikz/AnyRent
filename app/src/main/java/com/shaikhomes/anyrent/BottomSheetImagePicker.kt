package com.shaikhomes.anyrent

import android.app.Activity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shaikhomes.anyrent.databinding.DialogImagePickerBinding

class BottomSheetImagePicker{
    companion object {
        /**
         * showing bottom sheet
         * pass listener and implement two method
         *
         * @param context
         * @param listener
         *
         */
        fun showBottomSheet(
            context: Activity,
            listener: ImagePickerBottomSheetClickListener,
        ) {
            val dialog = BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme)
            val view =  DialogImagePickerBinding.inflate(context.layoutInflater)
            dialog.setContentView(view.root)
            view.txtImage.setOnClickListener {
                listener.imageClicked()
                dialog.dismiss()
            }
            view.txtCamera.setOnClickListener {
                dialog.dismiss()
                listener.cameraClicked()
            }
            view.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    /**
     * Bottom sheet  button click listener have two methods.
     *
     */
    interface ImagePickerBottomSheetClickListener {

        fun imageClicked()

        fun cameraClicked()
    }
}