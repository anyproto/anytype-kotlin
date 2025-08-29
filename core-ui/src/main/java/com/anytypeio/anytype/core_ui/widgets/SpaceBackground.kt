package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.core_ui.extensions.getWallpaperGradientByCode
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult
import timber.log.Timber


/**
 * Sealed class representing different background types for space icons
 */
sealed class SpaceBackground {
    data class SolidColor(val color: Color) : SpaceBackground()
    data class Gradient(val brush: Brush) : SpaceBackground()
    data object None : SpaceBackground()
}

/**
 * Computes the background for a SpaceIconView with priority:
 * 1. Wallpaper (gradient or color if available)
 * 2. Icon color for placeholders/images
 * 3. Default fallback
 *
 * @param icon The SpaceIconView to compute background for
 * @param wallpaperResult Wallpaper configuration
 */
@Composable
fun computeSpaceBackground(
    icon: SpaceIconView,
    wallpaperResult: WallpaperResult
): SpaceBackground {

    return when (wallpaperResult) {
        is WallpaperResult.Gradient -> {
            val gradient = getWallpaperGradientByCode(wallpaperResult.gradientCode)
            if (gradient != null) {
                SpaceBackground.Gradient(brush = gradient.toBrush())
            } else {
                SpaceBackground.None
            }
        }
        is WallpaperResult.SolidColor -> {
            try {
                SpaceBackground.SolidColor(
                    Color(android.graphics.Color.parseColor(wallpaperResult.colorHex))
                )
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Invalid color format: ${wallpaperResult.colorHex}")
                SpaceBackground.None
            }
        }
        WallpaperResult.None -> SpaceBackground.None
    }
}