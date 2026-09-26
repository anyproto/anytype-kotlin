package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorTabKeyTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        stubInterceptEvents()
        stubMove()
        stubUpdateText()
    }

    private fun text(
        style: Block.Content.Text.Style = Block.Content.Text.Style.P,
        children: List<String> = emptyList()
    ) = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = children,
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            marks = emptyList(),
            style = style
        )
    )

    private fun page(vararg children: Block) = Block(
        id = root,
        fields = Block.Fields.empty(),
        content = Block.Content.Smart,
        children = children.map { it.id }
    )

    private fun pressTab(block: Block, isShift: Boolean, times: Int = 1) {
        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        vm.onBlockFocusChanged(id = block.id, hasFocus = true)
        repeat(times) {
            vm.onKeyPressedEvent(
                KeyPressedEvent.OnTabKeyEvent(
                    target = block.id,
                    text = block.content<Block.Content.Text>().text,
                    marks = emptyList(),
                    isShift = isShift
                )
            )
        }
    }

    private fun verifyMove(block: Block, target: Block, position: Position) {
        verifyBlocking(move, times(1)) {
            async(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = target.id,
                    blockIds = listOf(block.id),
                    position = position
                )
            )
        }
    }

    @Test
    fun `tab moves the block into the previous sibling`() {
        val a = text(style = Block.Content.Text.Style.BULLET)
        val b = text(style = Block.Content.Text.Style.BULLET)
        stubOpenDocument(listOf(page(a, b), a, b))

        pressTab(block = b, isShift = false)

        verifyBlocking(move, times(1)) {
            async(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = a.id,
                    blockIds = listOf(b.id),
                    position = Position.INNER
                )
            )
        }
        clearPendingCoroutines()
    }

    @Test
    fun `tab does nothing for the first child`() {
        val a = text()
        val b = text()
        stubOpenDocument(listOf(page(a, b), a, b))

        pressTab(block = a, isShift = false)

        verifyBlocking(move, never()) { async(any()) }
        clearPendingCoroutines()
    }

    @Test
    fun `tab does nothing when the previous sibling cannot have children`() {
        val a = text(style = Block.Content.Text.Style.H1)
        val b = text()
        stubOpenDocument(listOf(page(a, b), a, b))

        pressTab(block = b, isShift = false)

        verifyBlocking(move, never()) { async(any()) }
        clearPendingCoroutines()
    }

    @Test
    fun `shift-tab moves the block below its parent`() {
        val child = text(style = Block.Content.Text.Style.NUMBERED)
        val parent = text(style = Block.Content.Text.Style.NUMBERED, children = listOf(child.id))
        stubOpenDocument(listOf(page(parent), parent, child))

        pressTab(block = child, isShift = true)

        verifyBlocking(move, times(1)) {
            async(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = parent.id,
                    blockIds = listOf(child.id),
                    position = Position.BOTTOM
                )
            )
        }
        clearPendingCoroutines()
    }

    @Test
    fun `shift-tab does nothing for a child of the page`() {
        val a = text()
        stubOpenDocument(listOf(page(a), a))

        pressTab(block = a, isShift = true)

        verifyBlocking(move, never()) { async(any()) }
        clearPendingCoroutines()
    }

    @Test
    fun `tab moves the block into a quote or a callout`() {
        listOf(Block.Content.Text.Style.QUOTE, Block.Content.Text.Style.CALLOUT).forEach { style ->
            val a = text(style = style)
            val b = text()
            stubOpenDocument(listOf(page(a, b), a, b))

            pressTab(block = b, isShift = false)

            verifyMove(block = b, target = a, position = Position.INNER)
            clearPendingCoroutines()
        }
    }

    @Test
    fun `tab does nothing for a header`() {
        val a = text()
        val b = text(style = Block.Content.Text.Style.H2)
        stubOpenDocument(listOf(page(a, b), a, b))

        pressTab(block = b, isShift = false)

        verifyBlocking(move, never()) { async(any()) }
        clearPendingCoroutines()
    }

    @Test
    fun `tab does nothing when the object restricts blocks`() {
        val a = text()
        val b = text()
        stubOpenDocument(
            document = listOf(page(a, b), a, b),
            objectRestrictions = listOf(ObjectRestriction.BLOCKS)
        )

        pressTab(block = b, isShift = false)

        verifyBlocking(move, never()) { async(any()) }
        clearPendingCoroutines()
    }

    @Test
    fun `a second tab waits until the first move is applied`() {
        val a = text()
        val b = text()
        stubOpenDocument(listOf(page(a, b), a, b))

        // The stubbed move returns no events, so the structure does not change.
        pressTab(block = b, isShift = false, times = 2)

        verifyMove(block = b, target = a, position = Position.INNER)
        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}
