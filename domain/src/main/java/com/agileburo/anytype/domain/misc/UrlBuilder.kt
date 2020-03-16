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
    fun image(hash: String?): Url = config.gateway + IMAGE_PATH + hash

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
    }
}