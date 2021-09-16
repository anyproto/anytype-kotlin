package com.anytypeio.anytype.core_utils.const

object MimeTypes {

    private const val PDF = "application/pdf"

    private val IMAGES = listOf(
        "image/jpeg",
        "image/png",
        "image/svg+xml",
        "image/webp",
        "image/gif",
        "image/avif",
        "image/apng",
        "image/bmp"
    )

    private val TEXTS = listOf(
        "text/plain",
        "text/csv",
        "text/html",
        "application/msword",
        "text/css",
        "application/x-csh",
    )

    private val VIDEOS = listOf(
        "video/mp4",
        "video/3gpp",
        "video/3gpp2",
        "video/H261",
        "video/H263",
        "video/mpv",
        "video/ogg",
        "video/x-msvideo"
    )

    private val AUDIOS = listOf(
        "audio/mp4",
        "audio/wave",
        "audio/wav",
        "audio/webm",
        "audio/ogg",
        "audio/aac",
        "audio/ac3",
        "audio/mpeg",
        "audio/midi",
        "audio/x-midi"
    )

    private val ARCHIVE = listOf(
        "application/zip",
        "application/zlib",
        "application/x-rar-compressed",
        "application/octet-stream",
        "application/x-zip-compressed",
        "multipart/x-zip",
        "application/x-bzip",
        "application/x-bzip2",
        "application/gzip"
    )

    enum class Category {
        IMAGE, PDF, TEXT, AUDIO, VIDEO, ARCHIVE, OTHER, TABLE, PRESENTATION
    }

    fun category(mime: String): Category = when {
        mime == PDF -> Category.PDF
        IMAGES.contains(mime) -> Category.IMAGE
        TEXTS.contains(mime) -> Category.TEXT
        VIDEOS.contains(mime) -> Category.VIDEO
        AUDIOS.contains(mime) -> Category.AUDIO
        ARCHIVE.contains(mime) -> Category.ARCHIVE
        else -> Category.OTHER
    }
}