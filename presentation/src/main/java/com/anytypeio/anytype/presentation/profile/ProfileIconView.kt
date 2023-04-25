package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider

sealed class ProfileIconView {
    object Loading : ProfileIconView()
    object Placeholder : ProfileIconView()
    data class Emoji(val unicode: String) : ProfileIconView()
    data class Image(val url: Url) : ProfileIconView()

    data class Gradient(val from: String, val to: String): ProfileIconView()
}

fun ObjectWrapper.Basic.profileIcon(builder: UrlBuilder, gradientProvider: SpaceGradientProvider): ProfileIconView = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        ProfileIconView.Image(builder.thumbnail(hash))
    }
    iconOption != null -> {
        iconOption?.let {
            val gradient = gradientProvider.get(it)
            ProfileIconView.Gradient(gradient.from, gradient.to)
        } ?: ProfileIconView.Placeholder
    }
    else -> ProfileIconView.Placeholder
}