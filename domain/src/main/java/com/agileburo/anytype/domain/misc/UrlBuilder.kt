package com.agileburo.anytype.domain.misc

import com.agileburo.anytype.domain.common.Url
import com.agileburo.anytype.domain.config.Config

/**
 * Helper class for building urls for files and images
 * @property config configuration properties
 */
class UrlBuilder(val config: Config) {

    /**
     * Builds image url for given [hash]
     */
    fun image(hash: String?): Url = config.gateway + IMAGE_PATH + hash + DEFAULT_WIDTH_PARAM

    /**
     * Builds small image url for given [hash]
     */
    fun thumbnail(hash: String?): Url = config.gateway + IMAGE_PATH + hash + THUMBNAIL_WIDTH_PARAM

    /**
     * Builds file url for given [hash]
     */
    fun file(hash: String?): Url = config.gateway + FILE_PATH + hash

    /**
     * Builds video url for given [hash]
     */
    fun video(hash: String?): Url = config.gateway + FILE_PATH + hash

    companion object {
        const val IMAGE_PATH = "/image/"
        const val FILE_PATH = "/file/"
        const val DEFAULT_WIDTH_PARAM = "?width=1080"
        const val THUMBNAIL_WIDTH_PARAM = "?width=540"
    }
}