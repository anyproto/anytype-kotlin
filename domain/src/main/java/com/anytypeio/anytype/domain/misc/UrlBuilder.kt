package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.config.Gateway

/**
 * Helper class for building urls for files and images
 * @property gateway gateway data
 */
class UrlBuilderImpl(val gateway: Gateway): UrlBuilder {

    /**
     * Builds large image url for given [path]
     */
    override fun large(path: String): Url = gateway.provide() + IMAGE_PATH + path + LARGE_WIDTH_PARAM

    /**
     * Builds original image url for given [path]
     */
    override fun original(path: String): Url = gateway.provide() + IMAGE_PATH + path

    /**
     * Builds small image url for given [path]
     */
    override fun thumbnail(path: String): Url = medium(path)

    /**
     * Builds medium image url for given [path]
     */
    override fun medium(path: String): Url = gateway.provide() + IMAGE_PATH + path + MEDIUM_WIDTH_PARAM

    /**
     * Builds file url for given [path]
     */
    override fun file(path: String): Url = gateway.provide() + FILE_PATH + path

    /**
     * Builds video url for given [path]
     */
    override fun video(path: String): Url = gateway.provide() + FILE_PATH + path

    companion object {
        const val IMAGE_PATH = "/image/"
        const val FILE_PATH = "/file/"
        const val LARGE_WIDTH_PARAM = "?width=1920"
        const val MEDIUM_WIDTH_PARAM = "?width=320"
    }
}