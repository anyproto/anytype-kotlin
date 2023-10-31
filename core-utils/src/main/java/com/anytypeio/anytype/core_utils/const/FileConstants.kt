package com.anytypeio.anytype.core_utils.const

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_utils.ext.Mimetype
import timber.log.Timber

object FileConstants {
    const val REQUEST_FILE_SAF_CODE = 2211
    const val REQUEST_MEDIA_CODE = 2212
    const val REQUEST_PROFILE_IMAGE_CODE = 2213

    fun Mimetype.hasPermission(context: Context): Boolean {
        return when (this) {
            Mimetype.MIME_VIDEO_ALL -> {
                val permission = getPermissionToRequestForVideos()
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ).let { result -> result == PackageManager.PERMISSION_GRANTED }
                Timber.d("hasExternalStoragePermission, hasPermission:$hasPermission for permission:$permission")
                return hasPermission
            }
            Mimetype.MIME_IMAGE_ALL -> {
                val permission = getPermissionToRequestForImages()
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ).let { result -> result == PackageManager.PERMISSION_GRANTED }
                Timber.d("hasExternalStoragePermission, hasPermission:$hasPermission for permission:$permission")
                return hasPermission
            }
            Mimetype.MIME_FILE_ALL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    true
                } else {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ).let { result -> result == PackageManager.PERMISSION_GRANTED }
                }
            }
        }
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