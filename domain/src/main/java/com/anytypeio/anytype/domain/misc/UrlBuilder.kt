package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.config.Gateway

/**
 * Helper class for building urls for files and images
 * @property gateway gateway data
 */
class UrlBuilder(val gateway: Gateway) {

    /**
     * Builds image url for given [path]
     */
    fun image(path: String): Url = gateway.provide() + IMAGE_PATH + path + DEFAULT_WIDTH_PARAM

    /**
     * Builds original image url for given [path]
     */
    fun original(path: String): Url = gateway.provide() + IMAGE_PATH + path

    /**
     * Builds small image url for given [path]
     */
    fun thumbnail(path: String): Url = gateway.provide() + IMAGE_PATH + path + THUMBNAIL_WIDTH_PARAM

    /**
     * Builds file url for given [path]
     */
    fun file(path: String): Url = gateway.provide() + FILE_PATH + path

    /**
     * Builds video url for given [path]
     */
    fun video(path: String): Url = gateway.provide() + FILE_PATH + path

    companion object {
        const val IMAGE_PATH = "/image/"
        const val FILE_PATH = "/file/"
        const val DEFAULT_WIDTH_PARAM = "?width=1080"
        const val THUMBNAIL_WIDTH_PARAM = "?width=540"
    }
}