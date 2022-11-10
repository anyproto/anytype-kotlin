package com.anytypeio.anytype.core_utils.tools

import android.graphics.Color
import com.google.gson.GsonBuilder

fun String.randomColor(): Int {
    var hash = 0
    for (i in indices) {
        hash = get(i).toInt() + ((hash.shl(5) - hash))
    }
    val h = (hash % 360).toFloat()
    return Color.HSVToColor(floatArrayOf(h, 0.5f, 0.9f))
}

private val gson by lazy { GsonBuilder().setPrettyPrinting().create() }

fun Any.toPrettyString(): String {
    return try {
        gson.toJson(this)
    } catch (e: Exception) {
        this.toString()
    }
}