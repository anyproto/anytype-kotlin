package com.anytypeio.anytype.core_models

data class UnsplashImage(
    val id: Id,
    val url: Url,
    val artist: Artist,
) {
    data class Artist(
        val name: String,
        val url: Url
    )
}