package com.anytypeio.anytype.presentation.wallpaper

sealed class WallpaperSelectView {
    sealed class Section : WallpaperSelectView() {
        object SolidColor: Section()
        object Gradient: Section()
    }
    data class Wallpaper(val item: WallpaperView) : WallpaperSelectView()
}
