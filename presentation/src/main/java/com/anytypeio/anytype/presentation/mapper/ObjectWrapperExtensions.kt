package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder


fun ObjectWrapper.Basic.getImagePath(urlBuilder: UrlBuilder): String? {
    val image = this.iconImage
    return if (image.isNullOrBlank()) {
        null
    } else {
        urlBuilder.image(iconImage)
    }
}

fun ObjectWrapper.Basic.getEmojiPath(): String? {
    val emoji = this.iconEmoji
    return if (emoji.isNullOrBlank()) {
        null
    } else {
        emoji
    }
}