package com.agileburo.anytype.data.auth.model

data class ImageEntity(
    val id: String,
    val sizes: List<Size>
) {
    enum class Size { SMALL, LARGE, THUMB }
}