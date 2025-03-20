package com.shaikhomes.anyrent.ui.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class ImagePicker  private constructor() {

    companion object {

        const val IMAGE_PICKER_REQUEST_CODE = 101
        const val IMAGE_PICKER_PERMISSION_REQUEST_CODE = 102
        const val NOTIFICATION_PERMISSION = 112

        private var permission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private var NotificationPermission =
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS
            )
        //}

        /**
         * image and camera related permission check
         *
         * @param context
         * @param fragment
         * @return boolean

         * @modifier sudhir kumar
         * @since 13/02/2020
         */
        fun isPermissionRequired(context: Context, fragment: Fragment): Boolean {
            if (!permission.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }) {
                fragment.requestPermissions(
                    permission,
                    IMAGE_PICKER_PERMISSION_REQUEST_CODE
                )
                return false
            }
            return true
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun isNotificationsPermissionRequired(context: Context, fragment: Fragment): Boolean {
            if (!NotificationPermission.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }) {
                fragment.requestPermissions(
                    NotificationPermission,
                    NOTIFICATION_PERMISSION
                )
                return false
            }
            return true
        }

        /**
         * image and camera related permission check
         *
         * @param context
         * @param activity
         * @return boolean

         * @modifier sudhir kumar
         * @since 13/02/2020
         */
        @TargetApi(Build.VERSION_CODES.M)
        fun isPermissionRequired(context: Context, activity: AppCompatActivity): Boolean {
            if (!permission.all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }) {
                activity.requestPermissions(permission, IMAGE_PICKER_PERMISSION_REQUEST_CODE)
                return false
            }
            return true
        }

        /**
         * Start ImagePicker activity
         *
         * @param context
         * @param fragment
         */
        fun startImagePicker(context: Context, fragment: Fragment) {
            fragment.startActivityForResult(
                imageChooserIntent(
                    context
                ), IMAGE_PICKER_REQUEST_CODE
            )
        }

        fun startImagePicker(
            context: Context, fragment: Fragment, gallery: Boolean? = false,
            camera: Boolean? = false
        ) {
            fragment.startActivityForResult(
                imageChooserIntent(
                    context, gallery = gallery, camera = camera
                ), IMAGE_PICKER_REQUEST_CODE
            )
        }

        /**
         * Start ImagePicker activity
         *
         * @param activity
         */
        fun startImagePicker(activity: AppCompatActivity) {
            activity.startActivityForResult(imageChooserIntent(activity), IMAGE_PICKER_REQUEST_CODE)
        }

        private fun imageChooserIntent(context: Context): Intent {
            val intentList = arrayListOf<Intent>()
            val packageManager = context.packageManager

            val cameraIntent =
                getCameraIntent(context)
            intentList.add(cameraIntent)

            val galleryIntents =
                getGalleryIntent(packageManager)
            if (galleryIntents.isNotEmpty()) {
                intentList.addAll(galleryIntents)
            }
            val targetIntent = if (intentList.isNotEmpty()) {
                intentList.removeAt(0)
            } else {
                Intent()
            }
            val chooserIntent = Intent.createChooser(targetIntent, "Select")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
            return chooserIntent
        }

        private fun imageChooserIntent(
            context: Context,
            gallery: Boolean? = false,
            camera: Boolean? = false
        ): Intent {
            val intentList = arrayListOf<Intent>()
            val packageManager = context.packageManager

            val cameraIntent =
                getCameraIntent(context)
            if (camera == true) {
                intentList.add(cameraIntent)
            }

            val galleryIntents =
                getGalleryIntent(packageManager)
            if (galleryIntents.isNotEmpty() && gallery == true) {
                intentList.addAll(galleryIntents)
            }
            val targetIntent = if (intentList.isNotEmpty()) {
                intentList.removeAt(0)
            } else {
                Intent()
            }
            val chooserIntent = Intent.createChooser(targetIntent, "Select")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
            return chooserIntent
        }

        private fun getGalleryIntent(packageManager: PackageManager): List<Intent> {
            val list = arrayListOf<Intent>()

            val galIntent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            val listGallery = packageManager.queryIntentActivities(galIntent, 0)
            for (res in listGallery) {
                val intent = Intent(galIntent)
                intent.component =
                    ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                intent.setPackage(res.activityInfo.packageName)
                list.add(intent)
            }
            return list
        }

        private fun getCameraIntent(context: Context): Intent {
            val outputFileUri = getImageOutputUri(context)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            }
            return intent
        }

        @Nullable
        private fun getImageOutputUri(context: Context): Uri? {
            var outputFileUri: Uri? = null
            val getImage = context.externalCacheDir
            if (getImage != null) {
                outputFileUri = FileProvider.getUriForFile(
                    context,
                    "com.shaikhomes.anyrent.provider",
                    File(getImage.path, "picked_image.jpg")
                )
            }
            return outputFileUri
        }

        /**
         * Returns the captured image uri.
         *
         * @param context
         * @param intent
         * @return
         */
        @Nullable
        fun parseImageUri(context: Context, intent: Intent?): Uri? {
            val isCamera = intent?.action == MediaStore.ACTION_IMAGE_CAPTURE
            return if (isCamera || intent?.data == null) getImageOutputUri(
                context
            ) else intent.data
        }
    }
}