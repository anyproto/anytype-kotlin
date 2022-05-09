package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions

class EditorEmptySpaceInteractionTest : EditorPresentationTestSetup() {

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should ignore outside click if document isn't started yet`() {
        val vm = buildViewModel()
        vm.onOutsideClicked()
        verifyZeroInteractions(createBlock)
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if page contains only title with icon`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val doc = listOf(page, header, title)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyBlocking(createBlock, times(1)) {
            invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = "",
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if page contains only title with icon and one non-empty paragraph`() {

        // SETUP

        val block = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyBlocking(createBlock, times(1)) {
            invoke(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = "",
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if the last block is a link block`() {

        // SETUP

        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTitleAndOnePageLinkBlock(
            rootId = root,
            titleBlockId = firstChild,
            pageBlockId = secondChild
        )

        stubInterceptEvents()
        stubOpenDocument(page)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyBlocking(createBlock, times(1)) {
            invoke(
                params = eq(
                    CreateBlock.Params(
                        target = "",
                        context = root,
                        position = Position.INNER,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    //@Test
    fun `should not create a new paragraph but focus the last empty block`() {

        // SETUP

        val style = Block.Content.Text.Style.values().filter { style ->
            style != Block.Content.Text.Style.TITLE || style != Block.Content.Text.Style.DESCRIPTION
        }.random()

        val pic = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.DONE
            ),
            fields = Block.Fields.empty(),
            children = emptyList()
        )

        val txt = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val doc = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(pic.id, txt.id)
            ),
            pic,
            txt
        )

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Checking that no text block is focused

        vm.state.test().assertValue { value ->
            check(value is ViewState.Success)
            value.blocks.none { it is BlockView.Text && it.isFocused }
        }

        vm.onOutsideClicked()

        verifyZeroInteractions(createBlock)

        // Checking that the last text block is focused and has empty text

        vm.state.test().assertValue { value ->
            check(value is ViewState.Success)
            val last = value.blocks.last()
            check(last is BlockView.Text || last is BlockView.Code)
            when (last) {
                is BlockView.Code -> last.text.isEmpty() && last.isFocused
                is BlockView.Text -> last.text.isEmpty() && last.isFocused
                else -> throw IllegalStateException()
            }
        }
    }

    @Test
    fun `should not create a new paragraph on outside-clicked event if object has restriction BLOCKS`() {

        // SETUP

        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTitleAndOnePageLinkBlock(
            rootId = root,
            titleBlockId = firstChild,
            pageBlockId = secondChild
        )

        stubInterceptEvents()
        stubOpenDocument(document = page, objectRestrictions = listOf(ObjectRestriction.BLOCKS))
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.onOutsideClicked()

        verifyZeroInteractions(createBlock)
    }
}