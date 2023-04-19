package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.ReplaceBlock
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
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

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun `should invoke create relation block  when target block not empty`() = runTest {

        // SETUP

        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubCreateBlock(root = root)
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        // TESTING

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

        vm.onSlashItemClicked(
            SlashItem.Relation(
                relation = SlashRelationView.Item(
                    view = ObjectRelationView.Default(
                        id = r2.id,
                        key = r2.key,
                        name = r2.name.orEmpty(),
                        value = value2,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
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

        verifyBlocking(createBlock, times(1)) { execute(params) }
    }

    @Test
    fun `should invoke replace block when target block is empty`() = runTest {

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
            content = Block.Content.Smart,
            children = listOf(header.id, a.id)
        )

        val doc = listOf(page, header, title, a)
        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubReplaceBlock()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        val vm = buildViewModel()

        // TESTING

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
                relation = SlashRelationView.Item(
                    view = ObjectRelationView.Default(
                        key = r3.key,
                        id = r3.id,
                        name = r3.name.orEmpty(),
                        value = value3,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
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

    @Test
    fun `before replacing current target should hide slash widget and show nav toolbar until new block is focused`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, a.id)
        )

        val doc = listOf(page, header, title, a)
        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubReplaceBlock()
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        storeOfRelations.merge(listOf(r1, r2, r3))

        val vm = buildViewModel()

        // TESTING

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
                relation = SlashRelationView.Item(
                    view = ObjectRelationView.Default(
                        id = r3.id,
                        key = r3.key,
                        name = r3.name.orEmpty(),
                        value = value3,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
                    )
                )
            )
        )

        vm.controlPanelViewState.test().assertValue { value ->
            !value.mainToolbar.isVisible && value.navigationToolbar.isVisible && !value.slashWidget.isVisible
        }
    }

    @Test
    fun `before creating a new block under current target should hide slash widget and show nav toolbar until new block is focused`() = runTest {

        // SETUP

        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relationObject("Ad")
        val r2 = MockTypicalDocumentFactory.relationObject("De")
        val r3 = MockTypicalDocumentFactory.relationObject("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubCreateBlock(root = root)
        stubSearchObjects()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        storeOfRelations.merge(
            listOf(r1, r2, r3)
        )

        val vm = buildViewModel()

        // TESTING

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
                relation = SlashRelationView.Item(
                    view = ObjectRelationView.Default(
                        key = r2.key,
                        id = r2.id,
                        name = r2.name.orEmpty(),
                        value = value2,
                        featured = true,
                        format = Relation.Format.SHORT_TEXT,
                        system = false
                    )
                )
            )
        )

        vm.controlPanelViewState.test().assertValue { value ->
            !value.mainToolbar.isVisible && value.navigationToolbar.isVisible && !value.slashWidget.isVisible
        }
    }

    @Test
    fun `should remove slash filter after adding new relation`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = StubRelationObject(name = "Ad")
        val r2 = StubRelationObject(name = "De")
        val r3 = StubRelationObject(name = "HJ")
        val objectRelations = listOf(r1, r2, r3)
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubUpdateText()
        stubCreateBlock(root = root)
        stubSearchObjects()
        stubOpenDocument(document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        vm.onStart(root)
        val selection = IntRange(1, 1)

        //TESTING

        vm.apply {
            onSelectionChanged(
                id = a.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )

            val view = BlockView.Text.Paragraph(
                id = a.id,
                text = a.content<TXT>().text
            )

            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 13
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = view.copy(text = "Jggig xtstdt /")
            )
            onSelectionChanged(
                id = a.id,
                selection = IntRange(14, 14)
            )
            onSlashItemClicked(SlashItem.Main.Relations)
            onSlashItemClicked(SlashItem.RelationNew)

            //open RelationListScreen
            //open RelationCreateScreen
            proceedWithAddingRelationToTarget(
                target = a.id,
                relationKey = MockDataFactory.randomUuid()
            )
        }

        val result = vm.blocks.find { it.id == a.id }

        assertEquals("Jggig xtstdt ", result?.content<TXT>()?.text)

        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}