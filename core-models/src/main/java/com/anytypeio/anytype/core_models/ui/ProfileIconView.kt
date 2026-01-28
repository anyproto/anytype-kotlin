package com.anytypeio.anytype.core_models.ui

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.UrlBuilder

/**
 * Represents the visual icon for a user profile in the UI.
 */
sealed class ProfileIconView {
    data object Loading : ProfileIconView()
    data class Placeholder(val name: String?) : ProfileIconView()
    data class Image(val url: Url) : ProfileIconView()
}

fun ObjectWrapper.Basic.profileIcon(builder: UrlBuilder): ProfileIconView = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        ProfileIconView.Image(builder.medium(hash))
    }

    else -> ProfileIconView.Placeholder(
        name = name?.trim()?.ifEmpty { null }
    )
}