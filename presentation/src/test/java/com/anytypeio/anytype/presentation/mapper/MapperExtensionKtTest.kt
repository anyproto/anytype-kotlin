package com.anytypeio.anytype.presentation.mapper

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class MapperExtensionKtTest {

    @Mock
    lateinit var gateway: Gateway

    private val urlBuilder: UrlBuilder get() = UrlBuilder(gateway)

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should return file block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "*/txt"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.FILE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type

        )

        val expected = BlockView.Media.File(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            mode = BlockView.Mode.EDIT,
            url = urlBuilder.video(hash),
            indent = indent,
            decorations = emptyList()
        )
        val actual = block.toFileView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder file block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.FILE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type
        )

        val expected =
            BlockView.MediaPlaceholder.File(id = id, indent = indent, isPreviousBlockMedia = false)
        val actual = block.toFileView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error file block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.FILE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.Error.File(id = id, indent = indent, decorations = emptyList())
        val actual = block.toFileView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload file block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.FILE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type
        )

        val expected = BlockView.Upload.File(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return picture block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "image/jpeg"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.IMAGE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type

        )

        val expected = BlockView.Media.Picture(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.image(hash),
            indent = indent,
            decorations = emptyList()
        )

        val actual = block.toPictureView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder picture block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.IMAGE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.MediaPlaceholder.Picture(
            id = id,
            indent = indent,
            isPreviousBlockMedia = false
        )
        val actual = block.toPictureView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error picture block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.IMAGE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.Error.Picture(
            id = id,
            indent = indent,
            decorations = emptyList()
        )

        val actual = block.toPictureView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload picture block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.IMAGE
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            state = state,
            type = type
        )

        val expected = BlockView.Upload.Picture(id = id, indent = indent)
        val actual = block.toPictureView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return video block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "video/mp4"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type
        )

        val expected = BlockView.Media.Video(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash),
            indent = indent,
            decorations = emptyList()
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return video block view empty params`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type

        )

        val expected = BlockView.Media.Video(
            id = id,
            name = null,
            size = null,
            mime = null,
            hash = null,
            url = urlBuilder.video(null),
            indent = indent,
            decorations = emptyList()
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder video block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.MediaPlaceholder.Video(
            id = id,
            indent = indent,
            isPreviousBlockMedia = false
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload video block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.Upload.Video(
            id = id,
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error video block view`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.Error.Video(
            id = id,
            indent = indent,
            decorations = emptyList()
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())

        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw exceptions when state not set`() {

        val id = MockDataFactory.randomUuid()

        val indent = MockDataFactory.randomInt()

        val type = Block.Content.File.Type.VIDEO
        val mode = BlockView.Mode.EDIT

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = null,
            type = type
        )

        block.toVideoView(id, urlBuilder, indent, mode, false, null, false, emptyList())
    }

    @Test
    fun `should not return mark when range from is equal text length`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 51..55,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should not return mark when range from is equal range to`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 15..15,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should not return mark when range to is less then zero`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = IntRange(15, -1),
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return mark when range from is less then zero`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = IntRange(-1, 10),
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 0..10,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return mark when range from is less then zero and to is bigger then text length`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = IntRange(-1, 55),
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 0..source.length,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return mark with swapped from and to ranges`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = IntRange(20, 10),
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 10..20,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return initial list of marks`() {
        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 10..20,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            ),
            Block.Content.Text.Mark(
                range = 30..50,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        val result = marks.filterByRange(source.length)

        val expected = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 10..20,
                param = "link",
                type = Block.Content.Text.Mark.Type.LINK
            ),
            Block.Content.Text.Mark(
                range = 30..50,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should not map mark if required param is blank`() {

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR
            ),
            Block.Content.Text.Mark(
                range = 10..20,
                type = Block.Content.Text.Mark.Type.BACKGROUND_COLOR
            ),
            Block.Content.Text.Mark(
                range = 30..50,
                type = Block.Content.Text.Mark.Type.LINK
            )
        )

        val content = Block.Content.Text(
            text = source,
            marks = marks,
            style = Block.Content.Text.Style.P
        )

        val result = content.marks(
            urlBuilder = urlBuilder,
            details = Block.Details(mapOf())
        )

        assertEquals(
            actual = result,
            expected = emptyList()
        )
    }

    @Test
    fun `should not map mention or object marks if required param is blank`() {

        val source = "Everything was in confusion in the Oblonskys’ house"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.MENTION
            ),
            Block.Content.Text.Mark(
                range = 10..20,
                type = Block.Content.Text.Mark.Type.BACKGROUND_COLOR,
                param = "blue"
            ),
            Block.Content.Text.Mark(
                range = 30..50,
                type = Block.Content.Text.Mark.Type.OBJECT,
                param = "      "
            )
        )

        val content = Block.Content.Text(
            text = source,
            marks = marks,
            style = Block.Content.Text.Style.P
        )

        val result = content.marks(
            urlBuilder = urlBuilder,
            details = Block.Details(mapOf())
        )

        assertEquals(
            actual = result,
            expected = listOf(
                Markup.Mark.BackgroundColor(
                    from = 10,
                    to = 20,
                    background = "blue"
                )
            )
        )
    }
}