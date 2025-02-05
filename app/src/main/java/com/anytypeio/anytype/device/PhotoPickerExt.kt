package com.anytypeio.anytype.device

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VisualMediaType
import androidx.fragment.app.Fragment
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.other.MediaPermissionHelper
import com.anytypeio.anytype.ui.editor.PickerDelegate
import timber.log.Timber

/**
 * Launches a media picker (for images or videos) in a [Fragment].
 *
 * This function checks if the device supports the photo picker. If available,
 * it launches the [pickMedia] launcher with a request for the specified [mediaType].
 * If the picker is not available, it falls back to opening a file picker using [pickerDelegate]
 * with the provided [fallbackMimeType].
 *
 * @param pickMedia The [ActivityResultLauncher] used to launch the media picker.
 * @param pickerDelegate A delegate to open a fallback file picker when the media picker is unavailable.
 * @param mediaType The type of media to pick (e.g. [PickVisualMedia.ImageOnly] or [PickVisualMedia.VideoOnly]).
 * @param fallbackMimeType The MIME type to use with the fallback file picker (e.g. [Mimetype.MIME_IMAGE_ALL] or [Mimetype.MIME_VIDEO_ALL]).
 */
fun Fragment.launchMediaPicker(
    pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    pickerDelegate: PickerDelegate,
    mediaType: VisualMediaType,
    fallbackMimeType: Mimetype
) {
    context?.let { ctx ->
        if (PickVisualMedia.isPhotoPickerAvailable(ctx)) {
            pickMedia.launch(PickVisualMediaRequest(mediaType))
        } else {
            Timber.w("$mediaType picker is not available, using pickerDelegate")
            pickerDelegate.openFilePicker(fallbackMimeType, null)
        }
    }
}

fun Fragment.launchMediaPicker(
    pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    permissionHelper: MediaPermissionHelper,
    mediaType: VisualMediaType,
    fallbackMimeType: Mimetype
) {
    context?.let { ctx ->
        if (PickVisualMedia.isPhotoPickerAvailable(ctx)) {
            pickMedia.launch(PickVisualMediaRequest(mediaType))
        } else {
            Timber.w("$mediaType picker is not available, using pickerDelegate")
            permissionHelper.openFilePicker(fallbackMimeType, null)
        }
    }
}