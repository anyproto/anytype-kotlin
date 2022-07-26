package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EditorSlashWidgetClicksTest: EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    /**
     * Testing clicks on menu items in Slash widget,
     * specifically for Actions see [EditorSlashWidgetActionsTest]
     * for Color & Background see [EditorSlashWidgetColorTest]
     * 1. Click on Style -> return sub header + back + style items
     * 9. Click on Style with view type Header -> return sub header + back + style items without bold and italic
     * 2. Click on Media -> return sub header + back + media items
     * 3. Click on Objects -> return sub header + back + page object type item(Because of Stable Build)
     * 4. Click on Relations -> return sub header + back + relation items
     * 5. Click on Other -> return sub header + back + other items
     * 6. Click on Actions -> return sub header + back + actions items
     * 7. Click on Alignment -> return sub header + back + alignment items
     * 8. Click on Alignment Numbers-> return sub header + back + alignment items empty
     */

    //region {STYLE}
    @Test
    fun `should return Update command with style items when click on style item`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Style)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = listOf(
                SlashItem.Subheader.StyleWithBack,
                SlashItem.Style.Type.Text,
                SlashItem.Style.Type.Title,
                SlashItem.Style.Type.Heading,
                SlashItem.Style.Type.Subheading,
                SlashItem.Style.Type.Highlighted,
                SlashItem.Style.Type.Callout,
                SlashItem.Style.Type.Checkbox,
                SlashItem.Style.Type.Bulleted,
                SlashItem.Style.Type.Numbered,
                SlashItem.Style.Type.Toggle,
                SlashItem.Style.Markup.Bold,
                SlashItem.Style.Markup.Italic,
                SlashItem.Style.Markup.Strikethrough,
                SlashItem.Style.Markup.Code
            ),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }

    @Test
    fun `should return Update command with style items without bold and italic`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_HEADER_TWO)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Style)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = listOf(
                SlashItem.Subheader.StyleWithBack,
                SlashItem.Style.Type.Text,
                SlashItem.Style.Type.Title,
                SlashItem.Style.Type.Heading,
                SlashItem.Style.Type.Subheading,
                SlashItem.Style.Type.Highlighted,
                SlashItem.Style.Type.Callout,
                SlashItem.Style.Type.Checkbox,
                SlashItem.Style.Type.Bulleted,
                SlashItem.Style.Type.Numbered,
                SlashItem.Style.Type.Toggle,
                SlashItem.Style.Markup.Strikethrough,
                SlashItem.Style.Markup.Code
            ),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {MEDIA}
    @Test
    fun `should return Update command with media items when click on media item`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Media)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = listOf(
                SlashItem.Subheader.MediaWithBack,
                SlashItem.Media.File,
                SlashItem.Media.Picture,
                SlashItem.Media.Video,
                SlashItem.Media.Bookmark,
                SlashItem.Media.Code
            ),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {OBJECTS}
    @Test
    fun `should return Update command with all object types item when click on Objects item`() {

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        val type1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        stubGetObjectTypes(
            objectTypes = listOf(type1, type2, type3)
        )

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Objects)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedObjectItems = listOf(
            SlashItem.Subheader.ObjectTypeWithBlack,
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

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = expectedObjectItems,
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )

        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {RELATIONS}
    @Test
    fun `should return Update command with relation items when clicked on Relations item`() {
        // SETUP

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Album's title",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Album's year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val relation3 = Relation(
            key = MockDataFactory.randomString(),
            name = "Album's artist",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.values().random()
        )

        val value1 = "Safe as milk"
        val value2 = 1967.0
        val value3 = "Captain Beefheart and his Magic Band"

        val customDetails =
            Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf(
                            relation1.key to value1,
                            relation2.key to value2,
                            relation3.key to value3
                        )
                    )
                )
            )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.RelationBlock(key = relation1.key)
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubInterceptEvents()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf(relation1, relation2, relation3)
        )

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
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
        }

        // TESTING

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Relations)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedRelationItems = listOf(
            SlashRelationView.Section.SubheaderWithBack,
            SlashRelationView.RelationNew,
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = relation1.key,
                    name = relation1.name,
                    value = value1,
                    format = relation1.format
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = relation2.key,
                    name = relation2.name,
                    value = NumberParser.parse(value2),
                    format = relation2.format
                )
            ),
            SlashRelationView.Item(
                view = DocumentRelationView.Default(
                    relationId = relation3.key,
                    name = relation3.name,
                    value = value3,
                    format = relation3.format
                )
            )
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = expectedRelationItems,
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {OTHERS}
    @Test
    fun `should return Update command with other items when click on Other item`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Other)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedOtherItems = listOf(
            SlashItem.Subheader.OtherWithBack,
            SlashItem.Other.Line,
            SlashItem.Other.Dots,
            SlashItem.Other.TOC,
            SlashItem.Other.Table()
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = expectedOtherItems,
            actionsItems = emptyList(),
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {ACTIONS}
    @Test
    fun `should return Update command with actions items when click on Action item(Stable Build)`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Actions)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedActionsItems = listOf(
            SlashItem.Subheader.ActionsWithBack,
            SlashItem.Actions.Delete,
            SlashItem.Actions.Duplicate,
            SlashItem.Actions.Copy,
            SlashItem.Actions.Paste,
            SlashItem.Actions.Move,
            SlashItem.Actions.MoveTo,
            SlashItem.Actions.LinkTo
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = expectedActionsItems,
            alignmentItems = emptyList(),
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

    //region {ALIGNMENT}
    @Test
    fun `should return Update command with alignment items when click on Alignment item`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_PARAGRAPH)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Alignment)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedAlignmentItems = listOf(
            SlashItem.Subheader.AlignmentWithBack,
            SlashItem.Alignment.Left,
            SlashItem.Alignment.Center,
            SlashItem.Alignment.Right
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = expectedAlignmentItems,
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }

    @Test
    fun `should return Update command with no alignment items when click on alignment item for numbers views`() {

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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Alignment)

        val state = vm.controlPanelViewState.value

        val stateWidget = state?.slashWidget?.widgetState as SlashWidgetState.UpdateItems

        assertNotNull(stateWidget)

        val expectedAlignmentItems = listOf(
            SlashItem.Subheader.AlignmentWithBack
        )

        val expected = SlashWidgetState.UpdateItems(
            mainItems = emptyList(),
            styleItems = emptyList(),
            mediaItems = emptyList(),
            objectItems = emptyList(),
            relationItems = emptyList(),
            otherItems = emptyList(),
            actionsItems = emptyList(),
            alignmentItems = expectedAlignmentItems,
            colorItems = emptyList(),
            backgroundItems = emptyList()
        )
        assertEquals(expected = expected, actual = stateWidget)
    }
    //endregion

}