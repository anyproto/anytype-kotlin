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
        "image/bmp",
        "image/tiff", // Added TIFF image type
        "image/vnd.adobe.photoshop", // Added PSD type
        "application/x-dwg" // Added DWG type
    )

    private val TEXTS = listOf(
        "text/plain", // .txt
        "text/csv",
        "text/html",  // .html
        "application/msword",  // .doc
        "text/css",
        "application/x-csh",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
        "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
        "application/json", // .json
        "application/ld+json",
        "text/comma-separated-values",
        "application/xml" // Added XML type
    )

    private val VIDEOS = listOf(
        "video/mp4",
        "video/3gpp",
        "video/3gpp2",
        "video/H261",
        "video/H263",
        "video/mpv",
        "video/ogg",
        "video/x-msvideo",
        "video/x-flv", // Added FLV type
        "video/x-ms-wmv", // Added WMV type
        "video/quicktime" // Added MOV type
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
        "audio/flac",
        "audio/x-aiff" // Added AIFF type
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
        "application/vnd.ms-excel", // .xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "text/csv" // Added CSV to table
    )

    private val PRESENTATION = listOf(
        "application/vnd.ms-powerpoint", // .ppt
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
        "application/vnd.openxmlformats-officedocument.presentationml.template", // Added PPTX Template
        "application/vnd.ms-powerpoint.template.macroEnabled.12", // Added PPT Template
        "application/vnd.ms-powerpoint.addin.macroEnabled.12" // Added PPT Addin
    )

    enum class Category {
        IMAGE, PDF, TEXT, AUDIO, VIDEO, ARCHIVE, OTHER, TABLE, PRESENTATION, PHOTO, BROKEN
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

    val MIME_EXTRA_YAML = arrayOf(
        "application/yaml",
        "application/yml",
        "text/yaml",
        "text/yml",
        "text/x-yaml",
        "application/zip",
        "application/x-zip",
        "application/octet-stream"
    )

    val MIME_EXTRA_IMAGE_VIDEO = arrayOf(
        "video/mp4",
        "video/3gpp",
        "video/3gpp2",
        "video/H261",
        "video/H263",
        "video/mpv",
        "video/ogg",
        "video/x-msvideo",
        "image/jpeg",
        "image/png",
        "image/svg+xml",
        "image/webp",
        "image/gif",
        "image/avif",
        "image/apng",
        "image/bmp"
    )
}