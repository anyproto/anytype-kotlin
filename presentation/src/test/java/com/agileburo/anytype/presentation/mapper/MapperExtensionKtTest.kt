package com.agileburo.anytype.presentation.mapper

import MockDataFactory
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.Gateway
import com.agileburo.anytype.domain.misc.UrlBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class MapperExtensionKtTest {

    @Mock
    lateinit var gateway : Gateway

    private val urlBuilder: UrlBuilder get() = UrlBuilder(gateway)

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
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
            indent = indent
        )
        val actual = block.toFileView(id, urlBuilder, indent, mode)

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

        val expected = BlockView.MediaPlaceholder.File(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent, mode)

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

        val expected = BlockView.Error.File(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent, mode)

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
        val actual = block.toFileView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toPictureView(id, urlBuilder, indent, mode)

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

        val expected = BlockView.MediaPlaceholder.Picture(id = id, indent = indent)
        val actual = block.toPictureView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toPictureView(id, urlBuilder, indent, mode)

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
        val actual = block.toPictureView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode)

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

        val actual = block.toVideoView(id, urlBuilder, indent, mode)

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
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent, mode)

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

        block.toVideoView(id, urlBuilder, indent, mode)
    }
}