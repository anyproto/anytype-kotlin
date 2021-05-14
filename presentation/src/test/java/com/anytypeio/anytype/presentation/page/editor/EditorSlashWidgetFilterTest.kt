package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.editor.model.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.page.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorSlashWidgetFilterTest : EditorPresentationTestSetup()  {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Test for SlashEvent.Filter
     * 1. Filter = "" -> SlashCommand.FilterItems.empty() +
     * 2. Filter = "b" -> SlashCommand.FilterItems.empty() +
     * 3. Filter = "/" -> SlashCommand.FilterItems with Main Items, other is empty +
     * 4. Filter = "/d" -> filter items by char 'd' +
     * 5. Filter = "Align r" +
     * 6. Filter = "bzxc" - close slash widget on three zero searches +
     */

    //region {1}
    @Test
    fun `should return empty Update command when filter is empty`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = SlashWidgetState.UpdateItems.empty()
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {2}
    @Test
    fun `should return empty Update command when filter is not started from slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "b", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = SlashWidgetState.UpdateItems.empty()
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {3}
    @Test
    fun `should return Update command with main items when filter only slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/", viewType = HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)

        val expected = SlashWidgetState.UpdateItems(
            mainItems = listOf(
                SlashItem.Main.Style,
                SlashItem.Main.Media,
                SlashItem.Main.Objects,
                SlashItem.Main.Relations,
                SlashItem.Main.Other,
                SlashItem.Main.Actions,
                SlashItem.Main.Alignment,
                SlashItem.Main.Color,
                SlashItem.Main.Background,
            ),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {4}
    @Test
    fun `should return Update command with filtered style items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedStyleItems = listOf(
            SlashItem.Subheader.Style,
            SlashItem.Style.Type.Heading,
            SlashItem.Style.Type.Subheading,
            SlashItem.Style.Type.Highlighted,
            SlashItem.Style.Type.Bulleted,
            SlashItem.Style.Type.Numbered,
            SlashItem.Style.Markup.Bold,
            SlashItem.Style.Markup.Code
        )
        assertEquals(expected = expectedStyleItems, actual = command.styleItems)
    }

    @Test
    fun `should return Update command with empty filtered style items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedStyleItems = emptyList<SlashItem>()
        assertEquals(expected = expectedStyleItems, actual = command.styleItems)
    }

    @Test
    fun `should return Update command with filtered media items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedMediaItems = listOf(
            SlashItem.Subheader.Media,
            SlashItem.Media.Video,
            SlashItem.Media.Code
        )
        assertEquals(expected = expectedMediaItems, actual = command.mediaItems)
    }

    @Test
    fun `should return Update command with empty filtered media items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.mediaItems)
    }

    @Test
    fun `should return Update command with filtered object items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.ObjectType,
            SlashItem.ObjectType(
                url = type1.url,
                name = type1.name,
                emoji = type1.emoji,
                description = type1.description,
                layout = type1.layout
            ),
            SlashItem.ObjectType(
                url = type2.url,
                name = type2.name,
                emoji = type2.emoji,
                description = type2.description,
                layout = type2.layout
            )
        )
        assertEquals(expected = expectedItems, actual = command.objectItems)
    }

    @Test
    fun `should return Update command with empty filtered object items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.objectItems)
    }

    @Test
    fun `should return Update command with filtered relation items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            RelationListViewModel.Model.Section.SlashWidget.Subheader,
            RelationListViewModel.Model.Item(
                view = DocumentRelationView.Default(
                    relationId = r1.key,
                    name = r1.name,
                    value = value1
                )
            ),
            RelationListViewModel.Model.Item(
                view = DocumentRelationView.Default(
                    relationId = r2.key,
                    name = r2.name,
                    value = value2.toString()
                )
            ),
        )
        assertEquals(expected = expectedItems, actual = command.relationItems)
    }

    @Test
    fun `should return Update command with empty filtered relation items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<RelationListViewModel.Model>()
        assertEquals(expected = expectedItems, actual = command.relationItems)
    }

    @Test
    fun `should return Update command with filtered other items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Dots
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `should return Update command with empty filtered other items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `should return Update command with filtered actions items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/d", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Actions,
            SlashItem.Actions.Delete,
            SlashItem.Actions.Duplicate
        )
        assertEquals(expected = expectedItems, actual = command.actionsItems)
    }

    @Test
    fun `should return Update command with empty filtered actions items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.actionsItems)
    }

    @Test
    fun `should return Update command with filtered alignment items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/r", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Alignment,
            SlashItem.Alignment.Center,
            SlashItem.Alignment.Right
        )
        assertEquals(expected = expectedItems, actual = command.alignmentItems)
    }

    @Test
    fun `should return Update command with empty filtered alignment items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.alignmentItems)
    }

    @Test
    fun `should return Update command with filtered color items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/r", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Color,
            SlashItem.Color.Text(
                code = ThemeColor.GREY.title,
                isSelected = false
            ),
            SlashItem.Color.Text(
                code = ThemeColor.ORANGE.title,
                isSelected = false
            ),
            SlashItem.Color.Text(
                code = ThemeColor.RED.title,
                isSelected = false
            ),
            SlashItem.Color.Text(
                code = ThemeColor.PURPLE.title,
                isSelected = false
            )
        )
        assertEquals(expected = expectedItems, actual = command.colorItems)
    }

    @Test
    fun `should return Update command with empty filtered color items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.colorItems)
    }

    @Test
    fun `should return Update command with filtered background items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/r", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Background,
            SlashItem.Color.Background(
                code = ThemeColor.GREY.title,
                isSelected = false
            ),
            SlashItem.Color.Background(
                code = ThemeColor.ORANGE.title,
                isSelected = false
            ),
            SlashItem.Color.Background(
                code = ThemeColor.RED.title,
                isSelected = false
            ),
            SlashItem.Color.Background(
                code = ThemeColor.PURPLE.title,
                isSelected = false
            )
        )
        assertEquals(expected = expectedItems, actual = command.backgroundItems)
    }

    @Test
    fun `should return Update command with empty filtered background items `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/z", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.backgroundItems)
    }
    //endregion

    //region {5}
    @Test
    fun `should return Update command with alignment right item `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/align r", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Alignment,
            SlashItem.Alignment.Right
        )
        assertEquals(expected = expectedItems, actual = command.alignmentItems)
    }
    //endregion

    //region {6}
    @Test
    fun `should send Stop event after 3 empty search results `() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val r1 = MockTypicalDocumentFactory.relation("Ad")
        val r2 = MockTypicalDocumentFactory.relation("De")
        val r3 = MockTypicalDocumentFactory.relation("HJ")
        val value1 = MockDataFactory.randomString()
        val value2 = MockDataFactory.randomString()
        val value3 = MockDataFactory.randomString()
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails, listOf(r1, r2, r3))

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/b", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/bz", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/bz3", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/bz3{", HOLDER_NUMBERED))
        val state = vm.controlPanelViewState.value
        val isVisible = state?.slashWidget?.isVisible
        assertNotNull(isVisible)
        assertFalse(isVisible)
    }
    //endregion

}