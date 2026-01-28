package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectType.Layout
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.objects.toViews
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class ObjectWrapperExtensionsKtTest {

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    lateinit var fieldParser: FieldParser

    val URL = "anytype.io/"

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
    }

    @Test
    fun `should return proper image url from object wrapper`() {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf("iconImage" to imageHash)
        )

        stubUrlBuilder(imageHash)

        val result = urlBuilder.large(obj.iconImage!!)

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

        val result = urlBuilder.large(obj.iconEmoji!!)

        assertNull(result)
    }

    @Test
    fun `should return null emoji from object wrapper when emoji is blank`() {

        val emojiHash: String = " "

        val obj = ObjectWrapper.Basic(
            mapOf("iconEmoji" to emojiHash)
        )

        val result = urlBuilder.large(obj.iconEmoji!!)

        assertNull(result)
    }

    @Test
    fun `should map to view with snippet as name when layout is note`() = runTest {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to "Ef6",
                Relations.SPACE_ID to MockDataFactory.randomUuid(),
                Relations.NAME to "LmL7R",
                Relations.SNIPPET to "OMr2Y",
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
            )

        )

        val result = listOf(obj).toViews(
            urlBuilder = urlBuilder,
            fieldParser,
            storeOfObjectTypes
        )

        assertEquals(
            expected = "OMr2Y",
            actual = result.first().name
        )
    }

    @Test
    fun `should map to view with name as name when layout is not note`() = runTest {

        val imageHash = "ycd79"

        val obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to "Ef6",
                Relations.SPACE_ID to MockDataFactory.randomUuid(),
                Relations.NAME to "LmL7R",
                Relations.SNIPPET to "OMr2Y",
                Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
            )

        )

        val result = listOf(obj).toViews(
            urlBuilder = urlBuilder,
            fieldParser,
            storeOfObjectTypes
        )

        assertEquals(
            expected = "LmL7R",
            actual = result.first().name
        )
    }

    @Test
    fun `should map to view proper snippet max 30 characters`() = runTest {

        val obj = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to "Ef6",
                Relations.SPACE_ID to MockDataFactory.randomUuid(),
                Relations.NAME to "LmL7R",
                Relations.SNIPPET to "Anytype\nis\nnext-generation software that\n" +
                        "works like\nyour brain does. It solves everyday\n" +
                        "problems\nwhile respecting your privacy and\n" +
                        "data rights.",
                Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble()
            )

        )

        val result = listOf(obj).toViews(
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )

        assertEquals(
            expected = "Anytype is next-generation sof",
            actual = result.first().name
        )
    }

    @Test
    fun testLegacyLayoutTakesPrecedence() {
        val details = mapOf(
            Relations.ID to "root",
            Relations.LEGACY_LAYOUT to 1.0, // should map to Layout.PROFILE
            Relations.LAYOUT to 3.0         // would map to Set, but legacy should win
        )
        val result = ObjectWrapper.Basic(details)
        val resultType = ObjectWrapper.Type(details)
        assertEquals(Layout.PROFILE, result.layout)
        assertEquals(Layout.PROFILE, resultType.layout)
    }

    @Test
    fun testResolvedLayoutUsedWhenLegacyAbsent() {
        val details = mapOf(
            Relations.ID to "root",
            Relations.LAYOUT to 3.0, // should map to Layout.Set
        )
        val result = ObjectWrapper.Basic(details)
        val resultType = ObjectWrapper.Type(details)
        assertEquals(Layout.SET, result.layout)
        assertEquals(Layout.SET, resultType.layout)
    }

    @Test
    fun testNonDoubleValueReturnsNull() {
        val details = mapOf(
            Relations.LEGACY_LAYOUT to "invalid"  // invalid value type
        )
        val result = ObjectWrapper.Basic(details)
        val resultType = ObjectWrapper.Type(details)
        assertNull(result.layout)
        assertNull(resultType.layout)
    }

    fun stubUrlBuilder(targetObjectId: String) {
        urlBuilder.stub {
            on { large(targetObjectId) } doReturn URL + targetObjectId
        }
    }
}