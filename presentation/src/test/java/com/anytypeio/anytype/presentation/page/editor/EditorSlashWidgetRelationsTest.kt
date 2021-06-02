package com.anytypeio.anytype.presentation.page.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.editor.model.Types
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorSlashWidgetRelationsTest: EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should invoke create relation block  when target block not empty`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubCreateBlock(root = root)
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        val selection = IntRange(1, 1)
        vm.apply {
            onSelectionChanged(
                id = a.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 1
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(
            SlashItem.Relation(
                relation = RelationListViewModel.Model.Item(
                    view = DocumentRelationView.Default(
                        relationId = r2.key,
                        name = r2.name,
                        value = value2,
                        isFeatured = true
                    )
                )
            )
        )

        val params = CreateBlock.Params(
            context = root,
            target = a.id,
            position = Position.BOTTOM,
            prototype = Block.Prototype.Relation(key = r2.key)
        )

        verifyBlocking(createBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should invoke replace block when target block is empty`() {
        // SETUP

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val doc = listOf(page, header, title, a)
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubReplaceBlock()
        stubGetObjectTypes(objectTypes = listOf())
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        val selection = IntRange(0, 0)

        vm.apply {
            onSelectionChanged(
                id = a.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = a.id,
                    text = "/",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(
            SlashItem.Relation(
                relation = RelationListViewModel.Model.Item(
                    view = DocumentRelationView.Default(
                        relationId = r3.key,
                        name = r3.name,
                        value = value3,
                        isFeatured = true
                    )
                )
            )
        )

        val params = ReplaceBlock.Params(
            context = root,
            target = a.id,
            prototype = Block.Prototype.Relation(key = r3.key)
        )

        verifyBlocking(replaceBlock, times(1)) { invoke(params) }
    }
}