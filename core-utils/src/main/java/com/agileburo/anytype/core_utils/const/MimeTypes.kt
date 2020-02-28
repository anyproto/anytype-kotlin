package com.agileburo.anytype.core_utils.const

object MimeTypes {

    private const val PDF = "application/pdf"

    private val IMAGES = listOf(
        "image/jpeg"
    )

    enum class Category {
        IMAGE, PDF, PICTURE, DOC, AUDIO, VIDEO, ZIP, OTHER
    }

    fun category(mime: String): Category = when {
        mime == PDF -> Category.PDF
        IMAGES.contains(mime) -> Category.IMAGE
        else -> Category.OTHER
    }
}