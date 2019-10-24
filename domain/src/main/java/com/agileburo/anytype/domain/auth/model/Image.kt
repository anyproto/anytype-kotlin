package com.agileburo.anytype.domain.auth.model

/**
 * @property id id of the image
 */
data class Image(
    val id: String,
    val sizes: List<Size>
) {
    enum class Size { SMALL, LARGE, THUMB }
}