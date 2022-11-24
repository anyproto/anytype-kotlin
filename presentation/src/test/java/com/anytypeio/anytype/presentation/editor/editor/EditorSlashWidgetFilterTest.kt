package com.anytypeio.anytype.presentation.editor.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.domain.table.CreateTable
import com.anytypeio.anytype.presentation.MockObjectTypes
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_HEADER_TWO
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_NUMBERED
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_PARAGRAPH
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.test.runTest
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
    //endregion

    //region {4}
    @Test
    fun `should return Update command with filtered style items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
            SlashItem.Style.Markup.Underline,
            SlashItem.Style.Markup.Code
        )
        assertEquals(expected = expectedStyleItems, actual = command.styleItems)
    }

    @Test
    fun `should return Update command with empty filtered style items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered media items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with empty filtered media items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(
                    key = it.key,
                    format = it.relationFormat
                )
            }
        )

        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered object items `() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockObjectTypes.objectTypePage
        val type2 = MockObjectTypes.objectTypeNote
        val type3 = MockObjectTypes.objectTypeTask
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        storeOfObjectTypes.merge(
            listOf(type1, type2, type3)
        )

        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/e", viewType = HOLDER_NUMBERED)
        vm.onSlashTextWatcherEvent(event = event)
        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(command)
        val expectedItems = listOf(
            SlashItem.Subheader.Actions,
            SlashItem.Actions.LinkTo,
            SlashItem.ObjectType(
                objectTypeView = ObjectTypeView(
                    id = type1.id,
                    name = type1.name.orEmpty(),
                    description = type1.description,
                    emoji = type1.iconEmoji
                )
            ),
            SlashItem.ObjectType(
                objectTypeView = ObjectTypeView(
                    id = type2.id,
                    name = type2.name.orEmpty(),
                    description = type2.description,
                    emoji = type2.iconEmoji
                )
            )
        )
        assertEquals(expected = expectedItems, actual = command.objectItems)
    }

    @Test
    fun `should return Update command with empty filtered object items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)
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
    fun `should return Update command with filtered relation items `() = runTest {
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
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
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
                    relationId = r1.id,
                    relationKey = r1.key,
                    name = r1.name.orEmpty(),
                    value = value1,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r2.id,
                    relationKey = r2.key,
                    name = r2.name.orEmpty(),
                    value = value2,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
        )
        assertEquals(expected = expectedItems, actual = command.relationItems)
    }

    @Test
    fun `should return Update command with empty filtered relation items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered other items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with empty filtered other items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered actions items `() = runTest{
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with empty filtered actions items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered alignment items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with empty filtered alignment items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered color items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
                themeColor = ThemeColor.GREY,
                isSelected = false
            ),
            SlashItem.Color.Text(
                themeColor = ThemeColor.ORANGE,
                isSelected = false
            ),
            SlashItem.Color.Text(
                themeColor = ThemeColor.RED,
                isSelected = false
            ),
            SlashItem.Color.Text(
                themeColor = ThemeColor.PURPLE,
                isSelected = false
            ),
        )
        assertEquals(expected = expectedItems, actual = command.colorItems)
    }

    @Test
    fun `should return Update command with empty filtered color items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with filtered background items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
                themeColor = ThemeColor.GREY,
                isSelected = false
            ),
            SlashItem.Color.Background(
                themeColor = ThemeColor.ORANGE,
                isSelected = false
            ),
            SlashItem.Color.Background(
                themeColor = ThemeColor.RED,
                isSelected = false
            ),
            SlashItem.Color.Background(
                themeColor = ThemeColor.PURPLE,
                isSelected = false
            ),
        )
        assertEquals(expected = expectedItems, actual = command.backgroundItems)
    }

    @Test
    fun `should return Update command with empty filtered background items `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should return Update command with alignment right item `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should send Stop event after 3 empty search results `() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should show all action items on action filter`() = runTest {
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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

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
            SlashItem.Actions.MoveTo
        )
        assertEquals(expected = expectedItems, actual = command.actionsItems)
    }
    //endregion

    //region {8}
    @Test
    fun `should show all media items on media filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)
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
    fun `should show all divider items on other filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

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
            SlashItem.Other.Dots,
            SlashItem.Other.TOC,
            SlashItem.Other.Table()
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }
    //endregion

    //region {10}
    @Test
    fun `should show all divider items on divider filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

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
    fun `should show all object items on objects filter`() = runTest {

        // SETUP

        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = MockObjectTypes.objectTypePage
        val type2 = MockObjectTypes.objectTypeNote
        val type3 = MockObjectTypes.objectTypeCustom
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects(
            listOf(type1, type2, type3).map { ObjectWrapper.Basic(it.map) }
        )
        stubOpenDocument(doc, customDetails)

        storeOfObjectTypes.merge(
            listOf(type1, type2, type3)
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/objects", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Actions,
            SlashItem.Actions.LinkTo,
            SlashItem.ObjectType(
                objectTypeView = ObjectTypeView(
                    id = type1.id,
                    name = type1.name.orEmpty(),
                    description = type1.description,
                    emoji = type1.iconEmoji
                )
            ),
            SlashItem.ObjectType(
                objectTypeView = ObjectTypeView(
                    id = type2.id,
                    name = type2.name.orEmpty(),
                    description = type2.description,
                    emoji = type2.iconEmoji
                )
            ),
            SlashItem.ObjectType(
                objectTypeView = ObjectTypeView(
                    id = type3.id,
                    name = type3.name.orEmpty(),
                    description = type3.description,
                    emoji = type3.iconEmoji
                )
            )
        )
        assertEquals(expected = expectedItems, actual = command.objectItems)
    }

    @Test
    fun `should show all relations items on relations filter`() = runTest {

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
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields(mapOf(r1.key to value1, r2.key to value2, r3.key to value3))
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails,
            relationLinks = objectRelations.map {
                RelationLink(key = it.key, format = it.relationFormat)
            }
        )

        val vm = buildViewModel()
        storeOfRelations.merge(objectRelations)
        storeOfObjectTypes.merge(objectTypes)

        // TESTING

        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/r", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/relations", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashRelationView.Section.Subheader,
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r1.id,
                    relationKey = r1.key,
                    name = r1.name.orEmpty(),
                    value = value1,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r2.id,
                    relationKey = r2.key,
                    name = r2.name.orEmpty(),
                    value = value2,
                    format = Relation.Format.SHORT_TEXT
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = r3.id,
                    relationKey = r3.key,
                    name = r3.name.orEmpty(),
                    value = value3,
                    format = Relation.Format.SHORT_TEXT
                )
            )
        )
        assertEquals(expected = expectedItems, actual = command.relationItems)
    }

    @Test
    fun `should return all unselected color items on color filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

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
            SlashItem.Color.Text(ThemeColor.DEFAULT, false),
            SlashItem.Color.Text(ThemeColor.GREY, false),
            SlashItem.Color.Text(ThemeColor.YELLOW, false),
            SlashItem.Color.Text(ThemeColor.ORANGE, false),
            SlashItem.Color.Text(ThemeColor.RED, false),
            SlashItem.Color.Text(ThemeColor.PINK, false),
            SlashItem.Color.Text(ThemeColor.PURPLE, false),
            SlashItem.Color.Text(ThemeColor.BLUE, false),
            SlashItem.Color.Text(ThemeColor.ICE, false),
            SlashItem.Color.Text(ThemeColor.TEAL, false),
            SlashItem.Color.Text(ThemeColor.LIME, false)
        )
        assertEquals(expected = expectedItems, actual = command.colorItems)
    }

    @Test
    fun `should return all unselected background items on background filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

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
            SlashItem.Color.Background(ThemeColor.DEFAULT, false),
            SlashItem.Color.Background(ThemeColor.GREY, false),
            SlashItem.Color.Background(ThemeColor.YELLOW, false),
            SlashItem.Color.Background(ThemeColor.ORANGE, false),
            SlashItem.Color.Background(ThemeColor.RED, false),
            SlashItem.Color.Background(ThemeColor.PINK, false),
            SlashItem.Color.Background(ThemeColor.PURPLE, false),
            SlashItem.Color.Background(ThemeColor.BLUE, false),
            SlashItem.Color.Background(ThemeColor.ICE, false),
            SlashItem.Color.Background(ThemeColor.TEAL, false),
            SlashItem.Color.Background(ThemeColor.LIME, false)
        )
        assertEquals(expected = expectedItems, actual = command.backgroundItems)
    }

    @Test
    fun `should return table of contents on toc filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/toc", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.TOC
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `should return table of contents and simple table on table filter`() = runTest {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val type1 = StubObjectType(name = "Hd")
        val type2 = StubObjectType(name = "Df")
        val type3 = StubObjectType(name = "LK")
        val objectTypes = listOf(type1, type2, type3)
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            details = customDetails
        )

        val vm = buildViewModel()
        storeOfObjectTypes.merge(objectTypes)

        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.TOC,
            SlashItem.Other.Table()
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item should have 9 rows and 7 columns`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table9x7", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Table(
                rowCount = 9,
                columnCount = 7
            )
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item should have 19 rows and default max columns`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table19x78t", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Table(
                rowCount = 19,
                columnCount = 25
            )
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item should have default max rows and default max columns`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table199x78", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Table(
                rowCount = CreateTable.DEFAULT_MAX_ROW_COUNT,
                columnCount = CreateTable.DEFAULT_MAX_COLUMN_COUNT
            )
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item should have 5 rows and default min columns`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table5", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Table(
                rowCount = 5,
                columnCount = CreateTable.DEFAULT_COLUMN_COUNT
            )
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item should have default max rows and default min columns`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/table26", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = listOf(
            SlashItem.Subheader.Other,
            SlashItem.Other.Table(
                rowCount = CreateTable.DEFAULT_MAX_ROW_COUNT,
                columnCount = CreateTable.DEFAULT_COLUMN_COUNT
            )
        )
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }

    @Test
    fun `slash item tables should not be present`() {
        // SETUP
        val doc = MockTypicalDocumentFactory.page(root)
        val a = MockTypicalDocumentFactory.a
        val fields = Block.Fields.empty()
        val customDetails = Block.Details(mapOf(root to fields))

        stubInterceptEvents()
        stubSearchObjects()
        stubOpenDocument(doc, customDetails)

        val vm = buildViewModel()
        vm.onStart(root)
        vm.apply {
            onBlockFocusChanged(a.id, true)
            onSlashTextWatcherEvent(SlashEvent.Start(100, 0))
        }

        // TESTING

        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/", HOLDER_NUMBERED))
        vm.onSlashTextWatcherEvent(SlashEvent.Filter("/tablee5x8", HOLDER_NUMBERED))

        val state = vm.controlPanelViewState.value
        val command = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        val expectedItems = emptyList<SlashItem>()
        assertEquals(expected = expectedItems, actual = command.otherItems)
    }
}