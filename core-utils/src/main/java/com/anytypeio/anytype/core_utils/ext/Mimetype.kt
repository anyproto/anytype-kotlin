package com.anytypeio.anytype.core_utils.ext

enum class Mimetype(val value: String) {
    MIME_TEXT_PLAIN("text/plain"),
    MIME_VIDEO_ALL("video/*"),
    MIME_IMAGE_ALL("image/*"),
    MIME_FILE_ALL("*/*"),
    MIME_IMAGE_AND_VIDEO("image/*,video/*"),
    MIME_YAML("application/zip"),
    MIME_APPLICATION_ALL("application/*")
}