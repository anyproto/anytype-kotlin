package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.domain.config.Gateway
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class UrlBuilderTest {

    @Mock
    lateinit var gateway: Gateway

    private lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        urlBuilder = UrlBuilder(gateway)
    }

    @Test
    fun `should return image url`() {
        val hash = "image001"

        val expected =
            gateway.provide() + UrlBuilder.IMAGE_PATH + hash + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when image hash is null`() {
        val hash = null

        val expected =
            gateway.provide() + UrlBuilder.IMAGE_PATH + null + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when image hash is empty`() {
        val hash = ""

        val expected = gateway.provide() + UrlBuilder.IMAGE_PATH + UrlBuilder.DEFAULT_WIDTH_PARAM
        val actual = urlBuilder.image(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return file url`() {
        val hash = "file001"

        val expected = gateway.provide() + UrlBuilder.FILE_PATH + hash
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when file hash is null`() {
        val hash = null

        val expected = gateway.provide() + UrlBuilder.FILE_PATH + null
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when file hash is empty`() {
        val hash = ""

        val expected = gateway.provide() + UrlBuilder.FILE_PATH
        val actual = urlBuilder.file(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return video url`() {
        val hash = "video001"

        val expected = gateway.provide() + UrlBuilder.FILE_PATH + hash
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url with null at the end when video hash is null`() {
        val hash = null

        val expected = gateway.provide() + UrlBuilder.FILE_PATH + null
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }

    @Test
    fun `should return url without hash when video hash is empty`() {
        val hash = ""

        val expected = gateway.provide() + UrlBuilder.FILE_PATH
        val actual = urlBuilder.video(hash)
        assertEquals(expected, actual)
    }
}