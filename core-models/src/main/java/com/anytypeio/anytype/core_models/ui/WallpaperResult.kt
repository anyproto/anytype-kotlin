package com.anytypeio.anytype.core_models.ui

/**
 * Result of wallpaper computation
 */
sealed class WallpaperResult {
    data class Gradient(val gradientCode: String) : WallpaperResult()
    data class SolidColor(val colorHex: String) : WallpaperResult()
    data object None : WallpaperResult()
}