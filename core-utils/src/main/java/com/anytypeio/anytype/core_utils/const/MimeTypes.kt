package com.anytypeio.anytype.core_utils.const

object MimeTypes {

    /** See
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
     */


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
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        "application/json",
        "application/ld+json",
        "text/comma-separated-values"
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
        "audio/x-wav",
        "audio/webm",
        "audio/ogg",
        "audio/aac",
        "audio/ac3",
        "audio/mpeg",
        "audio/midi",
        "audio/x-midi",
        "audio/m4a",
        "audio/mp3",
        "audio/x-flac",
        "audio/flac"
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
        "application/gzip",
        "application/vnd.rar",
        "application/java-archive",
        "application/x-tar",
        "application/x-7z-compressed"
    )

    private val TABLE = listOf(
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

    private val PRESENTATION = listOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )

    enum class Category {
        IMAGE, PDF, TEXT, AUDIO, VIDEO, ARCHIVE, OTHER, TABLE, PRESENTATION
    }

    fun category(mime: String?): Category = when {
        mime == PDF -> Category.PDF
        IMAGES.contains(mime) -> Category.IMAGE
        TEXTS.contains(mime) -> Category.TEXT
        VIDEOS.contains(mime) -> Category.VIDEO
        AUDIOS.contains(mime) -> Category.AUDIO
        ARCHIVE.contains(mime) -> Category.ARCHIVE
        TABLE.contains(mime) -> Category.TABLE
        PRESENTATION.contains(mime) -> Category.PRESENTATION
        else -> Category.OTHER
    }
}