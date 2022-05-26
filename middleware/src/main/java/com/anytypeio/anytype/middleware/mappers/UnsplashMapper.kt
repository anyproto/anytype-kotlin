package com.anytypeio.anytype.middleware.mappers

import anytype.Rpc.Unsplash.Search.Response.Picture
import com.anytypeio.anytype.core_models.UnsplashImage

fun Picture.core() : UnsplashImage = UnsplashImage(
    id = id,
    url = url,
    artist = UnsplashImage.Artist(
        name = artist,
        url = artistUrl
    )
)