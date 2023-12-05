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
            Mimetype.MIME_IMAGE_AND_VIDEO -> context.isPermissionGranted(getPermissionToRequestForImagesAndVideos())
            Mimetype.MIME_FILE_ALL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    true
                } else {
                    context.isPermissionGranted(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                }
            }
            else -> true
        }
    }

    private fun Context.isPermissionGranted(permission: Array<String>): Boolean {
        val hasPermission = permission.isNotEmpty() && ContextCompat.checkSelfPermission(this, permission[0]) == PackageManager.PERMISSION_GRANTED
        Timber.d("hasExternalStoragePermission, hasPermission:$hasPermission for permission:$permission")
        return hasPermission
    }

    fun Mimetype.getPermissionToRequestByMime(): Array<String> {
        return when (this) {
            Mimetype.MIME_VIDEO_ALL -> getPermissionToRequestForVideos()
            Mimetype.MIME_IMAGE_ALL -> getPermissionToRequestForImages()
            Mimetype.MIME_FILE_ALL -> getPermissionToRequestForFiles()
            Mimetype.MIME_IMAGE_AND_VIDEO -> getPermissionToRequestForImagesAndVideos()
            else -> {
                arrayOf()
            }
        }
    }

    fun getPermissionToRequestForImages(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun getPermissionToRequestForVideos(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    fun getPermissionToRequestForFiles(): Array<String> =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    fun getPermissionToRequestForImagesAndVideos(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
}
