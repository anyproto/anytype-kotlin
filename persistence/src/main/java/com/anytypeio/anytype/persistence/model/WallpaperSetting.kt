package com.anytypeio.anytype.persistence.model

import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.persistence.model.WallpaperSetting.Companion.WALLPAPER_TYPE_COLOR
import com.anytypeio.anytype.persistence.model.WallpaperSetting.Companion.WALLPAPER_TYPE_GRADIENT
import com.anytypeio.anytype.persistence.model.WallpaperSetting.Companion.WALLPAPER_TYPE_IMAGE

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperSetting(
    val type: Int,
    val value: String
) {
    companion object {
        const val WALLPAPER_TYPE_COLOR = 1
        const val WALLPAPER_TYPE_GRADIENT = 2
        const val WALLPAPER_TYPE_IMAGE = 3
    }
}

fun Wallpaper.asSettings(): WallpaperSetting? {
    return when (this) {
        is Wallpaper.Color -> {
            WallpaperSetting(
                type = WALLPAPER_TYPE_COLOR,
                value = code
            )
        }
        is Wallpaper.Gradient -> {
            WallpaperSetting(
                type = WALLPAPER_TYPE_GRADIENT,
                value = code
            )
        }
        is Wallpaper.Image -> {
            WallpaperSetting(
                type = WALLPAPER_TYPE_IMAGE,
                value = hash
            )
        }
        else -> null
    }
}

fun WallpaperSetting.asWallpaper(): Wallpaper {
    return when(type) {
        WALLPAPER_TYPE_COLOR -> {
            Wallpaper.Color(
                code = value
            )
        }
        WALLPAPER_TYPE_GRADIENT -> {
            Wallpaper.Gradient(
                code = value
            )
        }
        WALLPAPER_TYPE_IMAGE -> {
            Wallpaper.Image(
                hash = value
            )
        }
        else -> Wallpaper.Default
    }
}