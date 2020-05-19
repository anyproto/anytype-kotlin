package com.agileburo.anytype.presentation.mapper

import MockDataFactory
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.misc.UrlBuilder
import org.junit.Test
import kotlin.test.assertEquals

class MapperExtensionKtTest {

    @Test
    fun `should return file block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)
        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "*/txt"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.FILE

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type

        )

        val expected = BlockView.File.View(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash),
            indent = indent
        )
        val actual = block.toFileView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder file block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)
        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.FILE

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.File.Placeholder(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error file block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.FILE

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.File.Error(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload file block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.FILE

        val block = Block.Content.File(
            state = state,
            type = type
        )

        val expected = BlockView.File.Upload(id = id, indent = indent)
        val actual = block.toFileView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return picture block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "image/jpeg"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.IMAGE

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type

        )

        val expected = BlockView.Picture.View(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash),
            indent = indent
        )

        val actual = block.toPictureView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder picture block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)
        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.IMAGE

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.Picture.Placeholder(id = id, indent = indent)
        val actual = block.toPictureView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error picture block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.IMAGE

        val block = Block.Content.File(
            state = state,
            type = type

        )

        val expected = BlockView.Picture.Error(
            id = id,
            indent = indent
        )

        val actual = block.toPictureView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload picture block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.IMAGE

        val block = Block.Content.File(
            state = state,
            type = type
        )

        val expected = BlockView.Picture.Upload(id = id, indent = indent)
        val actual = block.toPictureView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return video block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)
        val indent = MockDataFactory.randomInt()

        val name = "name"
        val size = 10000L
        val mime = "video/mp4"
        val hash = "647tyhfgehf7ru"
        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            state = state,
            type = type
        )

        val expected = BlockView.Video.View(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash),
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return video block view empty params`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.DONE
        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type

        )

        val expected = BlockView.Video.View(
            id = id,
            name = null,
            size = null,
            mime = null,
            hash = null,
            url = urlBuilder.video(null),
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return placeholder video block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.EMPTY
        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.Video.Placeholder(
            id = id,
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return upload video block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.UPLOADING
        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.Video.Upload(
            id = id,
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return error video block view`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val state = Block.Content.File.State.ERROR
        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = state,
            type = type
        )

        val expected = BlockView.Video.Error(
            id = id,
            indent = indent
        )

        val actual = block.toVideoView(id, urlBuilder, indent)

        assertEquals(expected, actual)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw exceptions when state not set`() {

        val id = MockDataFactory.randomUuid()

        val config = Config(
            home = "home",
            gateway = "gateway",
            profile = "profile"
        )

        val urlBuilder = UrlBuilder(config = config)

        val indent = MockDataFactory.randomInt()

        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = null,
            type = type
        )

        block.toVideoView(id, urlBuilder, indent)
    }
}