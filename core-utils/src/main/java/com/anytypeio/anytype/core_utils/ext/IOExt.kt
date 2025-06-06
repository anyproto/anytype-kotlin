package com.anytypeio.anytype.core_utils.ext

import android.content.Context
import android.net.Uri
import java.io.IOException

fun Context.getJsonDataFromAsset(fileName: String): String? = try {
    assets
        .open(fileName)
        .bufferedReader()
        .use { stream -> stream.readText() }
} catch (e: IOException) {
    e.printStackTrace()
    null
}

fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}

fun isImage(uri: Uri, context: Context): Boolean {
    val mimeType = getMimeType(context, uri)
    return mimeType?.startsWith("image/") == true
}

fun isVideo(uri: Uri, context: Context): Boolean {
    val mimeType = getMimeType(context, uri)
    return mimeType?.startsWith("video/") == true
}