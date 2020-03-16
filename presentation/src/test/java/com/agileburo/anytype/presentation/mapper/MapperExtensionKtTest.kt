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
    fun `should return block view with type video`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

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

        val expected = BlockView.Video(
            id = id,
            name = name,
            size = size,
            mime = mime,
            hash = hash,
            url = urlBuilder.video(hash)
        )

        val actual = block.toVideoView(id, urlBuilder)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return block view with type video and empty params`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

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

        val expected = BlockView.Video(
            id = id,
            name = null,
            size = null,
            mime = null,
            hash = null,
            url = urlBuilder.video(null)
        )

        val actual = block.toVideoView(id, urlBuilder)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return block view with type empty`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

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

        val expected = BlockView.VideoEmpty(
            id = id
        )

        val actual = block.toVideoView(id, urlBuilder)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return block view with type upload`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

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

        val expected = BlockView.VideoUpload(
            id = id
        )

        val actual = block.toVideoView(id, urlBuilder)

        assertEquals(expected, actual)
    }

    @Test
    fun `should return block view with type error`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

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

        val expected = BlockView.VideoError(
            id = id
        )

        val actual = block.toVideoView(id, urlBuilder)

        assertEquals(expected, actual)
    }

    @Test(expected = NotImplementedError::class)
    fun `should throw NotImplementedError when state null`() {

        val id = MockDataFactory.randomUuid()
        val urlBuilder = UrlBuilder(config = Config(home = "home", gateway = "gateway"))

        val type = Block.Content.File.Type.VIDEO

        val block = Block.Content.File(
            name = null,
            size = null,
            mime = null,
            hash = null,
            state = null,
            type = type
        )

        block.toVideoView(id, urlBuilder)
    }
}