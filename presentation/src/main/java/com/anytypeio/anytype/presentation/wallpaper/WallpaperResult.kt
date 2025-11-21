package com.anytypeio.anytype.presentation.wallpaper

import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import timber.log.Timber

/**
 * Result of wallpaper computation
 */
sealed class WallpaperResult {
    data class Gradient(val gradientCode: String) : WallpaperResult()
    data class SolidColor(val colorHex: String) : WallpaperResult()
    data object None : WallpaperResult()
}

/**
 * Computes the wallpaper result with priority:
 * 1. Wallpaper (gradient or color if available)
 * 2. Icon color for background
 * 3. Default fallback color
 *
 * @param icon The SpaceIconView to compute background for
 * @param wallpaper Optional wallpaper configuration
 */
fun computeWallpaperResult(
    icon: SpaceIconView,
    wallpaper: Wallpaper? = null
): WallpaperResult {

    // First priority: Use wallpaper if available
    if (wallpaper != null) {
        when (wallpaper) {
            is Wallpaper.Gradient -> {
                return WallpaperResult.Gradient(wallpaper.code)
            }

            is Wallpaper.Color -> {
                val wallpaperColor = WallpaperColor.entries.find { it.code == wallpaper.code }
                if (wallpaperColor != null) {
                    return try {
                        // Validate the color format
                        WallpaperResult.SolidColor(wallpaperColor.hex)
                    } catch (e: IllegalArgumentException) {
                        // Handle invalid color format
                        Timber.w(e, "Invalid wallpaper color format: ${wallpaperColor.hex}")
                        WallpaperResult.SolidColor(WallpaperColor.ICE.hex)
                    }
                }
            }

            is Wallpaper.Image -> {
                // For images, we can't extract a color, skip to next priority
            }

            is Wallpaper.Default -> {
                val iconColor = getSpaceIconColor(icon)
                if (iconColor != null) {
                    val wallpaperColor = findClosestWallpaperColorForSystemColor(iconColor)
                    if (wallpaperColor != null) {
                        return WallpaperResult.SolidColor(wallpaperColor.hex)
                    }
                }
                return WallpaperResult.SolidColor(WallpaperColor.ICE.hex)
            }
        }
    }

    // Second priority: Use icon color if available
    val iconColor = getSpaceIconColor(icon)
    if (iconColor != null) {
        val wallpaperColor = findClosestWallpaperColorForSystemColor(iconColor)
        if (wallpaperColor != null) {
            return WallpaperResult.SolidColor(wallpaperColor.hex)
        }
    }

    // Default fallback
    return WallpaperResult.None
}

fun getSpaceIconColor(icon: SpaceIconView): SystemColor? {
    return when (icon) {
        is SpaceIconView.ChatSpace.Placeholder -> icon.color
        is SpaceIconView.DataSpace.Placeholder -> icon.color
        is SpaceIconView.ChatSpace.Image -> icon.color
        is SpaceIconView.DataSpace.Image -> icon.color
        SpaceIconView.Loading -> null
    }
}

private fun findClosestWallpaperColorForSystemColor(systemColor: SystemColor): WallpaperColor? {
    return when (systemColor) {
        SystemColor.GRAY -> WallpaperColor.LIGHT_GREY
        SystemColor.YELLOW -> WallpaperColor.YELLOW
        SystemColor.AMBER -> WallpaperColor.ORANGE
        SystemColor.RED -> WallpaperColor.RED
        SystemColor.PINK -> WallpaperColor.PINK
        SystemColor.PURPLE -> WallpaperColor.PURPLE
        SystemColor.BLUE -> WallpaperColor.BLUE
        SystemColor.SKY -> WallpaperColor.ICE
        SystemColor.TEAL -> WallpaperColor.TEAL
        SystemColor.GREEN -> WallpaperColor.GREEN
    }
}