package com.anytypeio.anytype.core_models

sealed class Wallpaper {
    object Default: Wallpaper()
    data class Color(val code: Id) : Wallpaper()
    data class Gradient(val code: Id) : Wallpaper()
    data class Image(val hash: Hash) : Wallpaper()
}