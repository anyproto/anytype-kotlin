package com.anytypeio.anytype.presentation.wallpaper

sealed class WallpaperView {
    data class SolidColor(val code: String) : WallpaperView()
    data class Gradient(val code: String) : WallpaperView()

    companion object {
        const val WALLPAPER_DEFAULT_ALPHA = 85
    }
}