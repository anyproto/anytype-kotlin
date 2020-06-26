package com.agileburo.anytype.core_utils.ext

import android.content.Context
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