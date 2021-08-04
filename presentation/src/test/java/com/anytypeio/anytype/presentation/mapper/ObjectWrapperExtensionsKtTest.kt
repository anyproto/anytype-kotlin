package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObjectWrapperExtensionsKtTest {

    @Mock
    lateinit var urlBuilder: UrlBuilder

    val URL = "anytype.io/"

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should return proper image url from object wrapper`() {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf("iconImage" to imageHash)
        )

        stubUrlBuilder(imageHash)

        val result = obj.getImagePath(urlBuilder)

        val expected = URL + imageHash

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return null image path from object wrapper when hash is null`() {

        val imageHash: String? = null

        val obj = ObjectWrapper.Basic(
            mapOf("iconImage" to imageHash)
        )

        stubUrlBuilder(imageHash)

        val result = obj.getImagePath(urlBuilder)

        assertNull(actual = result)
    }

    @Test
    fun `should return null image path from object wrapper when hash is empty`() {

        val imageHash = ""

        val obj = ObjectWrapper.Basic(
            mapOf("iconImage" to imageHash)
        )

        stubUrlBuilder(imageHash)

        val result = obj.getImagePath(urlBuilder)

        assertNull(actual = result)
    }

    @Test
    fun `should return null image path from object wrapper when hash is blank`() {

        val imageHash = " "

        val obj = ObjectWrapper.Basic(
            mapOf("iconImage" to imageHash)
        )

        stubUrlBuilder(imageHash)

        val result = obj.getImagePath(urlBuilder)

        assertNull(actual = result)
    }

    @Test
    fun `should return proper emoji from object wrapper`() {

        val emojiHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = obj.getEmojiPath()

        val expected = emojiHash

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is null`() {

        val emojiHash: String? = null

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = obj.getEmojiPath()

        assertNull(result)
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is empty`() {

        val emojiHash: String = ""

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = obj.getEmojiPath()

        assertNull(result)
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is blank`() {

        val emojiHash: String = " "

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = obj.getEmojiPath()

        assertNull(result)
    }

    fun stubUrlBuilder(hash: String?) {
        if (hash == null) {
            urlBuilder.stub {
                on { image(null) } doThrow RuntimeException("Should not happened")
            }
        } else {
            urlBuilder.stub {
                on { image(hash) } doReturn URL + hash
            }
        }
    }
}