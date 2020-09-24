package com.anytypeio.anytype.core_utils.const

object MimeTypes {

    private const val PDF = "application/pdf"

    private val IMAGES = listOf(
        "image/jpeg"
    )

    private val TEXTS = listOf(
        "text/plain"
    )

    enum class Category {
        IMAGE, PDF, TEXT, AUDIO, VIDEO, ARCHIVE, OTHER, TABLE, PRESENTATION
    }

    fun category(mime: String): Category = when {
        mime == PDF -> Category.PDF
        IMAGES.contains(mime) -> Category.IMAGE
        TEXTS.contains(mime) -> Category.TEXT
        else -> Category.OTHER
    }
}