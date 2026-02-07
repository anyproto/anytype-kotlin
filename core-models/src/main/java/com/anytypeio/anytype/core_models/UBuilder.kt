package com.anytypeio.anytype.core_models

interface UrlBuilder {
    fun large(path: String): Url
    fun original(path: String): Url
    fun thumbnail(path: String): Url
    fun medium(path: String): Url
    fun file(path: String): Url
    fun video(path: String): Url
}