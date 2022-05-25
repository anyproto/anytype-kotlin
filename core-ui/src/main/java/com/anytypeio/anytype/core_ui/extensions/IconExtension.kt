package com.anytypeio.anytype.core_ui.extensions

import android.widget.ImageView
import com.anytypeio.anytype.emojifier.Emojifier
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber

fun ImageView.setEmojiOrNull(unicode: String?) {
    if (unicode != null)
        try {
            Glide
                .with(this)
                .load(Emojifier.uri(unicode))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        } catch (e: Throwable) {
            Timber.e(e, "Error while setting emoji icon for: $unicode")
        }
    else
        setImageDrawable(null)
}

fun ImageView.setImageOrNull(image: String?) {
    if (image != null) {
        Glide
            .with(this)
            .load(image)
            .centerInside()
            .circleCrop()
            .into(this)
    } else {
        setImageDrawable(null)
    }
}