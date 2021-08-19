package com.anytypeio.anytype.presentation.editor.editor

import MockDataFactory
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
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
class EditorSlashWidgetFilterTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    /**
     * Test for SlashEvent.Filter
     * 1. Filter = "" -> SlashCommand.FilterItems.empty() +
     * 2. Filter = "b" -> SlashCommand.FilterItems.empty() +
     * 3. Filter = "/" -> SlashCommand.FilterItems with Main Items, other is empty +
     * 4. Filter = "/d" -> filter items by char 'd' +
     * 5. Filter = "Align r" +
     * 6. Filter = "bzxc" - close slash widget on three zero searches +
     * 7. Filter = "actions" - Subheader.Actions + Items.Action
     * 8. Filter = "media" - Subheader.Media + Items.Media
     * 9. Filter = "other" - Subheader.Divider + Items.Divider
     * 10. Filter = "divider" - Subheader.Divider + Items.Divider
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
    fun `should return Update command with relations in main items when filter only slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubOpenDocument(document = doc)
        stubGetFlavourConfig(isDataViewEnabled = true)
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

        val mainItems = listOf(
            SlashItem.Main.Style,
            SlashItem.Main.Media,
            SlashItem.Main.Objects,
            SlashItem.Main.Relations,
            SlashItem.Main.Other,
            SlashItem.Main.Actions,
            SlashItem.Main.Alignment,
            SlashItem.Main.Color,
            SlashItem.Main.Background,
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = mainItems,
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

    @Test
    fun `should return Update command with no relations in main items when filter only slash`() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubOpenDocument(document = doc)
        stubGetFlavourConfig(isDataViewEnabled = false)
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

        val mainItems = listOf(
            SlashItem.Main.Style,
            SlashItem.Main.Media,
            SlashItem.Main.Objects,
            SlashItem.Main.Other,
            SlashItem.Main.Actions,
            SlashItem.Main.Alignment,
            SlashItem.Main.Color,
            SlashItem.Main.Background,
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = mainItems,
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
            SlashRelationView.Section.Subheader,
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r1.key,
                    name = r1.name,
                    value = value1,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r2.key,
                    name = r2.name,
                    value = value2.toString(),
                    format = Relation.Format.SHORT_TEXT
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
        val expectedItems = emptyList<SlashRelationView>()
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
            SlashItem.Other.Line,
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

        val event = SlashEvent.Filter(filter = "/r", viewType = HOLDER_PARAGRAPH)
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

        val event = SlashEvent.Filter(filter = "/rig", viewType = HOLDER_HEADER_TWO)
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

    //region {7}
    @Test
    fun `should show all action items on action filter`() {
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

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/a", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/actions", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Actions,
            SlashItem.Actions.Delete,
            SlashItem.Actions.Duplicate,
            SlashItem.Actions.Copy,
            SlashItem.Actions.Paste,
            SlashItem.Actions.Move,
            SlashItem.Actions.MoveTo,
            SlashItem.Actions.LinkTo
        )
        assertEquals(expected = expectedItems, actual = command.actionsItems)
    }
    //endregion

    //region {8}
    @Test
    fun `should show all media items on media filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/media", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Media,
            SlashItem.Media.File,
            SlashItem.Media.Picture,
            SlashItem.Media.Video,
            SlashItem.Media.Bookmark,
            SlashItem.Media.Code
        )
        assertEquals(expected = expectedItems, actual = command.mediaItems)
    }
    //endregion

    //region {9}
    @Test
    fun `should show all divider items on other filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/other", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Line,
            SlashItem.Other.Dots
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }
    //endregion

    //region {10}
    @Test
    fun `should show all divider items on divider filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/divider", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Line,
            SlashItem.Other.Dots
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }
    //endregion

    @Test
    fun `should show all object items on objects filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/objects", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

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
            ),
            SlashItem.ObjectType(
                url = type3.url,
                name = type3.name,
                emoji = type3.emoji,
                description = type3.description,
                layout = type3.layout
            )
        )
        assertEquals(expected = expectedItems, actual = command.objectItems)
    }

    @Test
    fun `should show all relations items on relations filter`() {
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

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/r", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/relations", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashRelationView.Section.Subheader,
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r1.key,
                    name = r1.name,
                    value = value1,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r2.key,
                    name = r2.name,
                    value = value2,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r3.key,
                    name = r3.name,
                    value = value3,
                    format = Relation.Format.SHORT_TEXT
                )
            )
        )
        assertEquals(expected = expectedItems, actual = command.relationItems)
    }

    @Test
    fun `should return all unselected color items on color filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter(filter = "/", viewType = HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter(filter = "/color", viewType = HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Color,
            SlashItem.Color.Text(ThemeColor.DEFAULT.title, false),
            SlashItem.Color.Text(ThemeColor.GREY.title, false),
            SlashItem.Color.Text(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Text(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Text(ThemeColor.RED.title, false),
            SlashItem.Color.Text(ThemeColor.PINK.title, false),
            SlashItem.Color.Text(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Text(ThemeColor.BLUE.title, false),
            SlashItem.Color.Text(ThemeColor.ICE.title, false),
            SlashItem.Color.Text(ThemeColor.TEAL.title, false),
            SlashItem.Color.Text(ThemeColor.GREEN.title, false)
        )
        assertEquals(expected = expectedItems, actual = command.colorItems)
    }

    @Test
    fun `should return all unselected background items on background filter`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockTypicalDocumentFactory.objectType("Hd")
        val type2 = MockTypicalDocumentFactory.objectType("Df")
        val type3 = MockTypicalDocumentFactory.objectType("LK")
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubGetObjectTypes(listOf(type1, type2, type3))
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter(filter = "/", viewType = HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(
            SlashEvent.Filter(
                filter = "/background",
                viewType = HOLDER_NUMBERED
            )
        )

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Background,
            SlashItem.Color.Background(ThemeColor.DEFAULT.title, false),
            SlashItem.Color.Background(ThemeColor.GREY.title, false),
            SlashItem.Color.Background(ThemeColor.YELLOW.title, false),
            SlashItem.Color.Background(ThemeColor.ORANGE.title, false),
            SlashItem.Color.Background(ThemeColor.RED.title, false),
            SlashItem.Color.Background(ThemeColor.PINK.title, false),
            SlashItem.Color.Background(ThemeColor.PURPLE.title, false),
            SlashItem.Color.Background(ThemeColor.BLUE.title, false),
            SlashItem.Color.Background(ThemeColor.ICE.title, false),
            SlashItem.Color.Background(ThemeColor.TEAL.title, false),
            SlashItem.Color.Background(ThemeColor.GREEN.title, false)
        )
        assertEquals(expected = expectedItems, actual = command.backgroundItems)
    }
}