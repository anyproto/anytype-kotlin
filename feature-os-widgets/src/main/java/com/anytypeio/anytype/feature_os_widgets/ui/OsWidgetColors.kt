package com.anytypeio.anytype.feature_os_widgets.ui

import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.core_models.SystemColor

/**
 * Maps SystemColor to Compose Color for widget use.
 * These are hardcoded values matching the palette_system_* colors.
 */
fun SystemColor.toWidgetColor(): Color {
    return when (this) {
        SystemColor.GRAY -> Color(0xFF928F8E)
        SystemColor.YELLOW -> Color(0xFFE5D044)
        SystemColor.AMBER -> Color(0xFFF19611)
        SystemColor.RED -> Color(0xFFF55522)
        SystemColor.PINK -> Color(0xFFE51284)
        SystemColor.PURPLE -> Color(0xFFAB50CC)
        SystemColor.BLUE -> Color(0xFF3E58EB)
        SystemColor.SKY -> Color(0xFF2AA7EE)
        SystemColor.TEAL -> Color(0xFF0FC8BA)
        SystemColor.GREEN -> Color(0xFF5DD400)
    }
}

/**
 * Widget background color (matches background_primary)
 */
val OsWidgetBackgroundColor = Color(0xFF1F1E1D)

/**
 * Widget surface color for cards
 */
val OsWidgetSurfaceColor = Color(0xFF2B2A29)

/**
 * Widget text primary color
 */
val OsWidgetTextPrimary = Color(0xFFFFFFFF)

/**
 * Widget text secondary color
 */
val OsWidgetTextSecondary = Color(0xFFACA9A6)
