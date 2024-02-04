package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.objects.toView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
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

        val result = urlBuilder.image(obj.iconImage!!)

        val expected = URL + imageHash

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is empty`() {

        val emojiHash: String = ""

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = urlBuilder.image(obj.iconEmoji!!)

        assertNull(result)
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is blank`() {

        val emojiHash: String = " "

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = urlBuilder.image(obj.iconEmoji!!)

        assertNull(result)
    }

    @Test
    fun `should map to view with snippet as name when layout is note`() {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "Ef6",
                "name" to "LmL7R",
                "snippet" to "OMr2Y",
                "layout" to ObjectType.Layout.NOTE.code.toDouble()
            )

        )

        val result = listOf(obj).toView(urlBuilder, objectTypes = listOf())

        assertEquals(
            expected = "OMr2Y",
            actual = result.first().name
        )
    }

    @Test
    fun `should map to view with name as name when layout is not note`() {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "Ef6",
                "name" to "LmL7R",
                "snippet" to "OMr2Y",
                "layout" to ObjectType.Layout.BASIC.code.toDouble()
            )

        )

        val result = listOf(obj).toView(urlBuilder, objectTypes = listOf())

        assertEquals(
            expected = "LmL7R",
            actual = result.first().name
        )
    }

    @Test
    fun `should map to view proper snippet max 30 characters`() {

        val obj = ObjectWrapper.Basic(
            mapOf(
                "id" to "Ef6",
                "name" to "LmL7R",
                "snippet" to "Anytype\nis\nnext-generation software that\n" +
                        "works like\nyour brain does. It solves everyday\n" +
                        "problems\nwhile respecting your privacy and\n" +
                        "data rights.",
                "layout" to ObjectType.Layout.NOTE.code.toDouble()
            )

        )

        val result = listOf(obj).toView(urlBuilder, objectTypes = listOf())

        assertEquals(
            expected = "Anytype is next-generation sof",
            actual = result.first().name
        )
    }

    fun stubUrlBuilder(targetObjectId: String) {
        urlBuilder.stub {
            on { image(targetObjectId) } doReturn URL + targetObjectId
        }
    }
}