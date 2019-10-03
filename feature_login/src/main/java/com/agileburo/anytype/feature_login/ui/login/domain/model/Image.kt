package com.agileburo.anytype.feature_login.ui.login.domain.model

data class Image(
    val id: String,
    val size: ImageSize
) {
    enum class ImageSize {
        LARGE, SMALL, THUMB
    }
}