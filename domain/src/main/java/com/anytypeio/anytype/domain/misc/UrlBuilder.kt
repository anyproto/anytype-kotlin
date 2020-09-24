package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.domain.common.Url
import com.anytypeio.anytype.domain.config.Gateway

/**
 * Helper class for building urls for files and images
 * @property gateway gateway data
 */
class UrlBuilder(val gateway: Gateway) {

    /**
     * Builds image url for given [hash]
     */
    fun image(hash: String?): Url = gateway.obtain() + IMAGE_PATH + hash + DEFAULT_WIDTH_PARAM

    /**
     * Builds small image url for given [hash]
     */
    fun thumbnail(hash: String): Url = gateway.obtain() + IMAGE_PATH + hash + THUMBNAIL_WIDTH_PARAM

    /**
     * Builds file url for given [hash]
     */
    fun file(hash: String?): Url = gateway.obtain() + FILE_PATH + hash

    /**
     * Builds video url for given [hash]
     */
    fun video(hash: String?): Url = gateway.obtain() + FILE_PATH + hash

    companion object {
        const val IMAGE_PATH = "/image/"
        const val FILE_PATH = "/file/"
        const val DEFAULT_WIDTH_PARAM = "?width=1080"
        const val THUMBNAIL_WIDTH_PARAM = "?width=540"
    }
}