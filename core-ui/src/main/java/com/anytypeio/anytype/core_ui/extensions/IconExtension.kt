package com.anytypeio.anytype.core_ui.extensions

import android.widget.ImageView
import com.anytypeio.anytype.emojifier.Emojifier
import coil3.load
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import timber.log.Timber

fun ImageView.setEmojiOrNull(unicode: String?) {
    if (unicode != null)
        try {
            load(Emojifier.uri(unicode)) {
                memoryCachePolicy(coil3.request.CachePolicy.ENABLED)
                diskCachePolicy(coil3.request.CachePolicy.ENABLED)
            }
        } catch (e: Throwable) {
            Timber.w(e, "Error while setting emoji icon for: $unicode")
        }
    else
        setImageDrawable(null)
}

fun ImageView.setImageOrNull(image: String?) {
    if (image != null) {
        load(image) {
            transformations(CircleCropTransformation())
        }
    } else {
        setImageDrawable(null)
    }
}