package com.anytypeio.anytype.presentation.editor.editor.mention

import android.util.Log
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MentionExtTest {

    @Mock
    lateinit var gateway: Gateway

    lateinit var urlBuilder: UrlBuilder

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
    }

    @Test
    fun `should filter mentions by filter 1`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@C"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 2`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "abc",
            space = MockDataFactory.randomUuid(),
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "Cde",
            space = MockDataFactory.randomUuid(),
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            name = "EfB",
            space = MockDataFactory.randomUuid(),
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@CD"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention2)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 3`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@"

        val result = mentions.filterMentionsBy(filter)

        val expected = listOf(mention1, mention2, mention3)

        assertEquals(expected, result)
    }

    @Test
    fun `should filter mentions by filter 4`() {

        val mention1 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "abc"
        )

        val mention2 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "Cde"
        )

        val mention3 = DefaultObjectView(
            id = MockDataFactory.randomUuid(),
            space = MockDataFactory.randomUuid(),
            name = "EfB"
        )

        val mentions = listOf(mention1, mention2, mention3)

        val filter = "@EfB1"

        val result = mentions.filterMentionsBy(filter)

        val expected = emptyList<DefaultObjectView>()

        assertEquals(expected, result)
    }

    @Test
    fun `should create MentionLoading 1`() {

        val param = MockDataFactory.randomUuid()

        val mention = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj: ObjectWrapper.Basic? = null

        val result = mention.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Loading(
            from = mention.range.first,
            to = mention.range.last,
            param = param
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should create MentionLoading 2`() {

        val param = MockDataFactory.randomUuid()

        val mention = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj: ObjectWrapper.Basic? = null

        val result = mention.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Loading(
            from = mention.range.first,
            to = mention.range.last,
            param = param
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should create MentionDeleted`() {

        val param = MockDataFactory.randomUuid()

        val mention = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj: ObjectWrapper.Basic? = ObjectWrapper.Basic(
            mapOf(Relations.IS_DELETED to true)
        )

        val result = mention.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Deleted(
            from = mention.range.first,
            to = mention.range.last,
            param = param
        )

        assertEquals(expected, result)
    }

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @Test
    fun `should create base mention when layout is null`() {

        val param = MockDataFactory.randomString()

        val mark = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj = ObjectWrapper.Basic(mapOf())
        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Base(
            from = mark.range.first,
            to = mark.range.last,
            param = param,
            isArchived = false
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should create with emoji mention when layout is null and emoji is present`() {

        val param = MockDataFactory.randomString()

        val mark = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj = ObjectWrapper.Basic(mapOf(
            Relations.ICON_EMOJI to "emoji"
        ))
        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.WithEmoji(
            from = mark.range.first,
            to = mark.range.last,
            param = param,
            emoji = "emoji",
            isArchived = false
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should create with image mention when layout is null and image is present`() {

        val param = MockDataFactory.randomString()

        val mark = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj = ObjectWrapper.Basic(mapOf(
            Relations.ICON_IMAGE to "image"
        ))

        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.WithImage(
            from = mark.range.first,
            to = mark.range.last,
            param = param,
            image = urlBuilder.thumbnail("image"),
            isArchived = false
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should create mention marks according to object layout`() {

        val mark = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = MockDataFactory.randomString()
        )

        ObjectType.Layout.values().iterator().forEach { layout: ObjectType.Layout ->
            when (layout) {
                ObjectType.Layout.BASIC -> equalsMentionBase(mark, layout)
                ObjectType.Layout.PROFILE, ObjectType.Layout.PARTICIPANT -> equalsMentionProfile(mark, layout)
                ObjectType.Layout.TODO -> equalsMentionTodo(mark, layout)
                ObjectType.Layout.SET -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.COLLECTION -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.OBJECT_TYPE -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.RELATION -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.FILE -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.DASHBOARD -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.IMAGE -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.NOTE -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.SPACE -> equalsMentionNoIcon(mark, layout)
                ObjectType.Layout.PDF -> equalsMentionNoIcon(mark, layout)
                else -> {}
            }
        }
        equalsMentionBase(mark, null)
    }

    @Test
    fun `should create MentionBasic archived`() {

        val param = MockDataFactory.randomUuid()

        val mention = Block.Content.Text.Mark(
            range = IntRange(0, 10),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = param
        )

        val obj: ObjectWrapper.Basic? = ObjectWrapper.Basic(
            mapOf(Relations.IS_ARCHIVED to true)
        )

        val result = mention.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Base(
            from = mention.range.first,
            to = mention.range.last,
            param = param,
            isArchived = true
        )

        assertEquals(expected, result)
    }

    private fun equalsMentionBase(mark: Block.Content.Text.Mark, layout: ObjectType.Layout?) {
        val obj = ObjectWrapper.Basic(
            mapOf(
                Relations.LAYOUT to layout?.code?.toDouble()
            )
        )
        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Base(
            from = mark.range.first,
            to = mark.range.last,
            param = mark.param!!,
            isArchived = false
        )
        assertEquals(expected, result)
    }

    private fun equalsMentionProfile(mark: Block.Content.Text.Mark, layout: ObjectType.Layout) {
        val image = MockDataFactory.randomString()
        val obj = ObjectWrapper.Basic(
            mapOf(
                Relations.LAYOUT to layout.code.toDouble(),
                Relations.ICON_IMAGE to image
            )
        )
        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Profile.WithImage(
            from = mark.range.first,
            to = mark.range.last,
            imageUrl = urlBuilder.thumbnail(image),
            param = mark.param!!,
            isArchived = false
        )
        assertEquals(expected, result)

        val name = MockDataFactory.randomString()
        val objName = ObjectWrapper.Basic(
            mapOf(
                Relations.LAYOUT to layout.code.toDouble(),
                Relations.NAME to name
            )
        )
        val resultName = mark.createMentionMarkup(objName, urlBuilder)

        val expectedName = Markup.Mark.Mention.Profile.WithInitials(
            from = mark.range.first,
            to = mark.range.last,
            param = mark.param!!,
            initials = name.first(),
            isArchived = false
        )
        assertEquals(expectedName, resultName)
    }

    private fun equalsMentionTodo(mark: Block.Content.Text.Mark, layout: ObjectType.Layout) {
        val objTrue = ObjectWrapper.Basic(
            mapOf(
                Relations.LAYOUT to layout.code.toDouble(),
                Relations.DONE to true
            )
        )
        val resultTrue = mark.createMentionMarkup(objTrue, urlBuilder)

        val objFalse = ObjectWrapper.Basic(
            mapOf(
                Relations.LAYOUT to layout.code.toDouble(),
                Relations.DONE to false
            )
        )
        val resultFalse = mark.createMentionMarkup(objFalse, urlBuilder)

        val objNull = ObjectWrapper.Basic(
            mapOf(Relations.LAYOUT to layout.code.toDouble())
        )
        val resultNull = mark.createMentionMarkup(objNull, urlBuilder)

        val expectedTrue = Markup.Mark.Mention.Task.Checked(
            from = mark.range.first,
            to = mark.range.last,
            param = mark.param!!,
            isArchived = false
        )

        val expectedFalse = Markup.Mark.Mention.Task.Unchecked(
            from = mark.range.first,
            to = mark.range.last,
            param = mark.param!!,
            isArchived = false
        )
        assertEquals(expectedTrue, resultTrue)
        assertEquals(expectedFalse, resultFalse)
        assertEquals(expectedFalse, resultNull)
    }

    private fun equalsMentionNoIcon(mark: Block.Content.Text.Mark, layout: ObjectType.Layout) {
        val obj = ObjectWrapper.Basic(
            mapOf(Relations.LAYOUT to layout.code.toDouble())
        )
        val result = mark.createMentionMarkup(obj, urlBuilder)

        val expected = Markup.Mark.Mention.Base(
            from = mark.range.first,
            to = mark.range.last,
            param = mark.param!!,
            isArchived = false
        )
        assertEquals(expected, result)
    }
}