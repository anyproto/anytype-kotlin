package com.anytypeio.anytype.other

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.getPermissionToRequestByMime
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.hasPermission
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.core_utils.ext.toast
import timber.log.Timber

class MediaPermissionHelper(
    private val fragment: Fragment,
    private val onPermissionDenied: () -> Unit,
    private val onPermissionSuccess: (Mimetype, Int?) -> Unit
) {
    private var mimeType: Mimetype? = null
    private var requestCode: Int? = null
    private var isRequestInProgress: Boolean = false

    private val permissionReadStorage: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            Timber.d("Permission callback: $grantResults")

            if (mimeType == null) {
                Timber.e("mimeType is null in permission callback")
                onPermissionDenied()
                isRequestInProgress = false
                return@registerForActivityResult
            }

            val allGranted = grantResults.values.all { it }
            if (allGranted) {
                onPermissionSuccess(mimeType!!, requestCode)
            } else {
                onPermissionDenied()
            }

            // Reset state
            mimeType = null
            requestCode = null
            isRequestInProgress = false
        }

    fun openFilePicker(mimeType: Mimetype, requestCode: Int?) {
        Timber.d("openFilePicker, mimeType:$mimeType, requestCode:$requestCode")
        if (isRequestInProgress) {
            Timber.w("Permission request already in progress")
            return
        }

        try {
            this.mimeType = mimeType
            this.requestCode = requestCode
            isRequestInProgress = true

            val context = fragment.context ?: run {
                onPermissionDenied()
                isRequestInProgress = false
                return
            }

            val hasPermission = mimeType.hasPermission(context)
            if (hasPermission) {
                Timber.d("Permission already granted")
                onPermissionSuccess(mimeType, requestCode)
                isRequestInProgress = false
            } else {
                val permissions = mimeType.getPermissionToRequestByMime()
                Timber.d("Requesting permissions: $permissions")
                if (permissions.isNotEmpty()) {
                    permissionReadStorage.launch(permissions)
                } else {
                    // No permissions to request
                    onPermissionDenied()
                    isRequestInProgress = false
                }
            }
        } catch (e: Exception) {
            fragment.toast(e.msg())
            onPermissionDenied()
            isRequestInProgress = false
        }
    }
}