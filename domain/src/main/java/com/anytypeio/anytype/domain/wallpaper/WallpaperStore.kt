package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Wallpaper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface WallpaperStore {
    fun set(wallpaper: Wallpaper)
    fun get() : Wallpaper
    fun observe() : Flow<Wallpaper>

    object Default : WallpaperStore {
        private val wallpaper = MutableStateFlow<Wallpaper>(Wallpaper.Default)
        override fun set(wallpaper: Wallpaper) { this.wallpaper.value = wallpaper }
        override fun get(): Wallpaper = wallpaper.value
        override fun observe(): Flow<Wallpaper> = wallpaper
    }
}