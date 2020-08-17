package com.agileburo.anytype.domain.misc

import com.agileburo.anytype.domain.config.Config
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UrlBuilderTest {

    private lateinit var config: Config
    private lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        config = Config(
            home = "67889",
            gateway = "https://anytype.io",
            profile = "profile"
        )
        urlBuilder = UrlBuilder(config)
    }

    @Test
    fun `should return image url`() {
        val hash = "image001"

        val expected =
            config.gateway + UrlBuilder.IMAGE_PATH + hash + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when image hash is null`() {
        val hash = null

        val expected =
            config.gateway + UrlBuilder.IMAGE_PATH + null + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when image hash is empty`() {
        val hash = ""

        val expected = config.gateway + UrlBuilder.IMAGE_PATH + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return file url`() {
        val hash = "file001"

        val expected = config.gateway + UrlBuilder.FILE_PATH + hash
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when file hash is null`() {
        val hash = null

        val expected = config.gateway + UrlBuilder.FILE_PATH + null
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when file hash is empty`() {
        val hash = ""

        val expected = config.gateway + UrlBuilder.FILE_PATH
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return video url`() {
        val hash = "video001"

        val expected = config.gateway + UrlBuilder.FILE_PATH + hash
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when video hash is null`() {
        val hash = null

        val expected = config.gateway + UrlBuilder.FILE_PATH + null
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when video hash is empty`() {
        val hash = ""

        val expected = config.gateway + UrlBuilder.FILE_PATH
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }
}