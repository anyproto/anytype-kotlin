package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SplitBlockTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repository: BlockRepository

    lateinit var splitBlock: SplitBlock

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        splitBlock = SplitBlock(repository)
    }

    @Test
    fun `list block checkbox with children should return list style and inner`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.CHECKBOX,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.CHECKBOX,
                    range = range,
                    mode = BlockSplitMode.INNER
                )
            )
        }
    }

    @Test
    fun `list block checkbox without children should return list style and bottom`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.CHECKBOX,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.CHECKBOX,
                    range = range,
                    mode = BlockSplitMode.BOTTOM
                )
            )
        }
    }

    @Test
    fun `list block numbered with children should return list style and inner`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.NUMBERED,
                    range = range,
                    mode = BlockSplitMode.INNER
                )
            )
        }
    }

    @Test
    fun `list block numbered without children should return list style and bottom`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.NUMBERED,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.NUMBERED,
                    range = range,
                    mode = BlockSplitMode.BOTTOM
                )
            )
        }
    }

    @Test
    fun `list block bullet with children should return list style and inner`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.BULLET,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.BULLET,
                    range = range,
                    mode = BlockSplitMode.INNER
                )
            )
        }
    }

    @Test
    fun `list block bullet without children should return list style and bottom`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.BULLET,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.BULLET,
                    range = range,
                    mode = BlockSplitMode.BOTTOM
                )
            )
        }
    }

    @Test
    fun `toggle block opened should return paragraph and inner`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.TOGGLE,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, true))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.P,
                    range = range,
                    mode = BlockSplitMode.INNER
                )
            )
        }
    }

    @Test
    fun `toggle block closed should return toggle and bottom`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.TOGGLE,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.TOGGLE,
                    range = range,
                    mode = BlockSplitMode.BOTTOM
                )
            )
        }
    }

    @Test
    fun `paragraph block with children should return paragraph style and inner`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = listOf(MockDataFactory.randomString()),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.P,
                    range = range,
                    mode = BlockSplitMode.INNER
                )
            )
        }
    }

    @Test
    fun `paragraph block without children should return paragraph style and bottom`() {
        runBlocking {
            val context = MockDataFactory.randomUuid()
            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                content = Block.Content.Text(
                    text = "FooBar",
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )

            val range = 1..1

            splitBlock.run(SplitBlock.Params(context, block, range, false))

            verify(repository, times(1)).split(
                Command.Split(
                    context = context,
                    target = block.id,
                    style = Block.Content.Text.Style.P,
                    range = range,
                    mode = BlockSplitMode.BOTTOM
                )
            )
        }
    }
}