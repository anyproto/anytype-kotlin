package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.core_ui.extensions.getWallpaperGradientByCode
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult
import timber.log.Timber

/**
 * Sealed class representing different background types for Compose rendering
 */
sealed class SpaceBackground {
    data class SolidColor(val color: Color) : SpaceBackground()
    data class Gradient(val brush: Brush) : SpaceBackground()
    data object None : SpaceBackground()
}

/**
 * Extension function to convert WallpaperResult to SpaceBackground for Compose rendering
 */
@Composable
fun WallpaperResult.toSpaceBackground(): SpaceBackground {
    return when (this) {
        is WallpaperResult.Gradient -> {
            val gradient = getWallpaperGradientByCode(this.gradientCode)
            if (gradient != null) {
                SpaceBackground.Gradient(brush = gradient.toBrush())
            } else {
                SpaceBackground.None
            }
        }
        is WallpaperResult.SolidColor -> {
            try {
                SpaceBackground.SolidColor(
                    Color(android.graphics.Color.parseColor(this.colorHex))
                )
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Invalid color format: ${this.colorHex}")
                SpaceBackground.None
            }
        }
        WallpaperResult.None -> SpaceBackground.None
    }
}

/**
 * Legacy function for backwards compatibility - will be removed after migration
 * @deprecated Use WallpaperResult.toSpaceBackground() instead
 */
@Deprecated("Use WallpaperResult.toSpaceBackground() instead")
@Composable
fun computeSpaceBackground(
    icon: com.anytypeio.anytype.presentation.spaces.SpaceIconView,
    wallpaperResult: WallpaperResult
): SpaceBackground = wallpaperResult.toSpaceBackground()