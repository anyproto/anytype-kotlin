package com.anytypeio.anytype.core_utils.const

import android.Manifest
import android.os.Build

object FileConstants {
    const val REQUEST_FILE_SAF_CODE = 2211
    const val REQUEST_MEDIA_CODE = 2212
    const val REQUEST_PROFILE_IMAGE_CODE = 2213

    fun getPermissionToRequestForImages() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun getPermissionToRequestForVideos() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}