package com.anytypeio.anytype.other

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.getPermissionToRequestByMime
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.getPermissionToRequestForFiles
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.getPermissionToRequestForImages
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.getPermissionToRequestForVideos
import com.anytypeio.anytype.core_utils.ext.FilePickerUtils.hasPermission
import com.anytypeio.anytype.core_utils.ext.Mimetype

class MediaPermissionHelper(
    private val fragment: Fragment,
    private val onPermissionDenied: () -> Unit,
    private val onPermissionSuccess: (Mimetype, Int?) -> Unit
) {
    private var mimeType: Mimetype? = null
    private var requestCode: Int? = null

    private val permissionReadStorage: ActivityResultLauncher<Array<String>>

    init {
        permissionReadStorage = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
            val readResult = when (mimeType) {
                Mimetype.MIME_VIDEO_ALL -> grantResults[getPermissionToRequestForVideos()]
                Mimetype.MIME_IMAGE_ALL -> grantResults[getPermissionToRequestForImages()]
                Mimetype.MIME_FILE_ALL -> grantResults[getPermissionToRequestForFiles()]
                null -> false
            }
            if (readResult == true) {
                val type = requireNotNull(mimeType) {
                    "mimeType should be initialized"
                }
                onPermissionSuccess(type, requestCode)
            } else {
                onPermissionDenied()
            }
        }
    }

    fun openFilePicker(mimeType: Mimetype, requestCode: Int?) {
        this.mimeType = mimeType
        this.requestCode = requestCode
        val context = fragment.context ?: return
        val hasPermission = mimeType.hasPermission(context)
        if (hasPermission) {
            onPermissionSuccess(mimeType, requestCode)
        } else {
            val permission = mimeType.getPermissionToRequestByMime()
            permissionReadStorage.launch(arrayOf(permission))
        }
    }
}