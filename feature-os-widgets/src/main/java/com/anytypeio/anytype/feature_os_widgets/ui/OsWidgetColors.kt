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
 * Maps SystemColor to light text color for icon initials.
 * These are contrasting colors that work well on top of system colors.
 */
fun SystemColor.toWidgetLightTextColor(): Color {
    return when (this) {
        SystemColor.GRAY -> Color(0xFFD6D5D4)
        SystemColor.YELLOW -> Color(0xFFFCF6CE)
        SystemColor.AMBER -> Color(0xFFFEECCE)
        SystemColor.RED -> Color(0xFFFED6C9)
        SystemColor.PINK -> Color(0xFFF9CFEB)
        SystemColor.PURPLE -> Color(0xFFEBD4F3)
        SystemColor.BLUE -> Color(0xFFD2DAF8)
        SystemColor.SKY -> Color(0xFFD2EEFA)
        SystemColor.TEAL -> Color(0xFFC6F2EE)
        SystemColor.GREEN -> Color(0xFFDAF5B0)
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

/**
 * Widget text tertiary color (more muted)
 */
val OsWidgetTextTertiary = Color(0xFF6B6966)

/**
 * Widget icon placeholder background color
 */
val OsWidgetIconPlaceholderColor = Color(0xFF3D3C3B)

/**
 * Corner radius for space icons (matches vault 40dp icon -> 6dp radius)
 */
const val SPACE_ICON_CORNER_RADIUS = 6
