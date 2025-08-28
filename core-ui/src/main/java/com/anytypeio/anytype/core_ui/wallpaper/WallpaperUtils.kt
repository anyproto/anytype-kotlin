package com.anytypeio.anytype.core_ui.wallpaper

import android.graphics.Color
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import timber.log.Timber

/**
 * Utility for computing wallpaper backgrounds with priority-based fallbacks
 */
object WallpaperUtils {

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
     * 2. Icon color for placeholders/images
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
                            Color.parseColor(wallpaperColor.hex)
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

    private fun getSpaceIconColor(icon: SpaceIconView): SystemColor? {
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
}

/**
 * Gets the drawable resource ID for a gradient code
 */
@androidx.annotation.DrawableRes
fun getGradientDrawableResource(gradientCode: String): Int {
    return when (gradientCode) {
        CoverGradient.YELLOW -> R.drawable.cover_gradient_yellow
        CoverGradient.RED -> R.drawable.cover_gradient_red
        CoverGradient.BLUE -> R.drawable.cover_gradient_blue
        CoverGradient.TEAL -> R.drawable.cover_gradient_teal
        CoverGradient.PINK_ORANGE -> R.drawable.wallpaper_gradient_1
        CoverGradient.BLUE_PINK -> R.drawable.wallpaper_gradient_2
        CoverGradient.GREEN_ORANGE -> R.drawable.wallpaper_gradient_3
        CoverGradient.SKY -> R.drawable.wallpaper_gradient_4
        else -> R.drawable.cover_gradient_default
    }
}