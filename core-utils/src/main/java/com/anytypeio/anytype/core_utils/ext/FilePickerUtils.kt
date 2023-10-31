package com.anytypeio.anytype.core_utils.ext

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import timber.log.Timber

object FilePickerUtils {

    fun Mimetype.hasPermission(context: Context): Boolean {
        return when (this) {
            Mimetype.MIME_VIDEO_ALL -> context.isPermissionGranted(getPermissionToRequestForVideos())
            Mimetype.MIME_IMAGE_ALL -> context.isPermissionGranted(getPermissionToRequestForImages())
            Mimetype.MIME_FILE_ALL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    true
                } else {
                    context.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    fun Context.isPermissionGranted(permission: String): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        Timber.d("hasExternalStoragePermission, hasPermission:$hasPermission for permission:$permission")
        return hasPermission
    }

    fun Mimetype.getPermissionToRequestByMime(): String {
        return when (this) {
            Mimetype.MIME_VIDEO_ALL -> getPermissionToRequestForVideos()
            Mimetype.MIME_IMAGE_ALL -> getPermissionToRequestForImages()
            Mimetype.MIME_FILE_ALL -> getPermissionToRequestForFiles()
        }
    }

    fun getPermissionToRequestForImages(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun getPermissionToRequestForVideos(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun getPermissionToRequestForFiles(): String =
        Manifest.permission.READ_EXTERNAL_STORAGE
}
