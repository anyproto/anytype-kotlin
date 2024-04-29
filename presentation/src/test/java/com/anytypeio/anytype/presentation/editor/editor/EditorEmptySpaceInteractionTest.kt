package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.StubCodeSnippet
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLinkToObjectBlock
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubAnalyticSpaceHelperDelegate()
    }

    @Test
    fun `should ignore outside click if document isn't started yet`() = runTest {
        val vm = buildViewModel()
        vm.onOutsideClicked()
        advanceUntilIdle()
        verifyNoInteractions(createBlock)
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if page contains only title with icon`() = runTest {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id)
        )

        val doc = listOf(page, header, title)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
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
    fun `should create a new paragraph on outside-clicked event if page contains only title with icon and one non-empty paragraph`() = runTest {

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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubOpenDocument(doc)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
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
    fun `should create a new paragraph on outside-clicked event if the last block is a link block`() = runTest {

        // SETUP

        val link = StubLinkToObjectBlock()

        val root = StubSmartBlock(
            id = root,
            children = listOf(
                header.id,
                link.id
            )
        )

        val doc = listOf(
            root, header, title, link
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(doc)
        stubCreateBlock(root.id)
        stubGetNetworkMode()

        val vm = buildViewModel()

        vm.onStart(id = root.id, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
                params = eq(
                    CreateBlock.Params(
                        target = "",
                        context = root.id,
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
    fun `should not create a new paragraph on outside-clicked event if object has restriction BLOCKS`() = runTest {

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

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyNoInteractions(createBlock)
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if the last block is a table block`() = runTest {

        // SETUP

        val table = StubTable(children = listOf())
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = Block(
            id = root,
            children = listOf(header.id) + listOf(table.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val document = listOf(page, header, title, table)

        stubInterceptEvents()
        stubOpenDocument(document)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
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

    @Test
    fun `should create a new paragraph on outside-clicked event if the last block is a code snippet block`() = runTest {

        // SETUP

        val snippet = StubCodeSnippet(children = listOf())
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = Block(
            id = root,
            children = listOf(header.id) + listOf(snippet.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart
        )

        val document = listOf(page, header, title, snippet)

        stubInterceptEvents()
        stubOpenDocument(document)
        stubCreateBlock(root)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        vm.onOutsideClicked()

        advanceUntilIdle()

        verifyBlocking(createBlock, times(1)) {
            async(
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
}