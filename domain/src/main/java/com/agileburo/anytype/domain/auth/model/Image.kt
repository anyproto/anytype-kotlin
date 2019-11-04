package com.agileburo.anytype.domain.auth.model

/**
 * @property id id of the image
 */
data class Image(
    val id: String,
    val sizes: List<Size>
) {
    enum class Size { SMALL, LARGE, THUMB }

    val smallest: Size?
        get() = if (sizes.isNotEmpty()) {
            when {
                sizes.contains(Size.SMALL) -> Size.SMALL
                sizes.contains(Size.THUMB) -> Size.THUMB
                else -> Size.LARGE
            }
        } else {
            null
        }
}