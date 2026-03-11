package com.anytypeio.anytype.feature_os_widgets.ui

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.glance.ImageProvider
import java.io.File

internal fun loadCachedImageProvider(filePath: String): ImageProvider? {
    val bitmap = try {
        val file = File(filePath)
        if (file.exists()) BitmapFactory.decodeFile(filePath) else null
    } catch (_: Exception) {
        null
    } ?: return null

    // Do NOT recycle the bitmap here — Glance needs it alive until
    // RemoteViews are fully built (which happens asynchronously).
    return ImageProvider(bitmap)
}

internal fun getWidgetIconColor(iconOption: Int?, defaultColor: Color): Color {
    return when (iconOption) {
        1 -> OsWidgetIconGray
        2 -> OsWidgetIconYellow
        3 -> OsWidgetIconAmber
        4 -> OsWidgetIconRed
        5 -> OsWidgetIconPink
        6 -> OsWidgetIconPurple
        7 -> OsWidgetIconBlue
        8 -> OsWidgetIconSky
        9 -> OsWidgetIconTeal
        10 -> OsWidgetIconGreen
        else -> defaultColor
    }
}

internal fun stableItemId(input: String): Long {
    var hash = 1125899906842597L
    for (char in input) {
        hash = 31 * hash + char.code
    }
    return hash
}
