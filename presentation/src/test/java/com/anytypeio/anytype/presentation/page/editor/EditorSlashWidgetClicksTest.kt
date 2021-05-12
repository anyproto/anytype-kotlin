package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.page.editor.model.Types
import com.anytypeio.anytype.presentation.page.editor.slash.SlashCommand
import com.anytypeio.anytype.presentation.page.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
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
        MockitoAnnotations.initMocks(this)
    }

    /**
     * Testing clicks on menu items in Slash widget,
     * specifically for Actions see [EditorSlashWidgetActionsTest]
     * for Color & Background see [EditorSlashWidgetColorTest]
     * 1. Click on Style -> return sub header + back + style items
     * 2. Click on Media -> return sub header + back + media items
     * 3. Click on Objects -> return sub header + back + object type items
     * 4. Click on Relations -> return sub header + back + relation items
     * 5. Click on Other -> return sub header + back + other items
     * 6. Click on Actions -> return sub header + back + actions items
     * 7. Click on Alignment -> return sub header + back + alignment items
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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expected = SlashCommand.UpdateItems(
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
                SlashItem.Style.Markup.Breakthrough,
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
        assertEquals(expected = expected, actual = command)
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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {OBJECTS}
    @Test
    fun `should return Update command with object type items when click on Objects item`() {
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
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
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
            isHidden = MockDataFactory.randomBoolean()
        )

        val type2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean()
        )

        val type3 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean()
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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expectedObjectItems = listOf(
            SlashItem.Subheader.ObjectTypeWithBlack,
            SlashItem.ObjectType(
                url = type1.url,
                name = type1.name,
                emoji = type1.emoji,
                description = type1.description
            ),
            SlashItem.ObjectType(
                url = type2.url,
                name = type2.name,
                emoji = type2.emoji,
                description = type2.description
            ),
            SlashItem.ObjectType(
                url = type3.url,
                name = type3.name,
                emoji = type3.emoji,
                description = type3.description
            )
        )

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)

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
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expectedRelationItems = listOf(
            RelationListViewModel.Model.Section.NoSection,
            RelationListViewModel.Model.Item(
                view = DocumentRelationView.Default(
                    relationId = relation1.key,
                    name = relation1.name,
                    value = value1
                )
            ),
            RelationListViewModel.Model.Item(
                view = DocumentRelationView.Default(
                    relationId = relation2.key,
                    name = relation2.name,
                    value = value2.toString()
                )
            ),
            RelationListViewModel.Model.Item(
                view = DocumentRelationView.Default(
                    relationId = relation3.key,
                    name = relation3.name,
                    value = value3
                )
            )
        )

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)
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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expectedOtherItems = listOf(
            SlashItem.Subheader.OtherWithBack,
            SlashItem.Other.Line,
            SlashItem.Other.Dots
        )

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)
    }
    //endregion

    //region {ACTIONS}
    @Test
    fun `should return Update command with actions items when click on Action item`() {

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

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expectedActionsItems = listOf(
            SlashItem.Subheader.ActionsWithBack,
            SlashItem.Actions.Delete,
            SlashItem.Actions.Duplicate,
            SlashItem.Actions.Copy,
            SlashItem.Actions.Paste,
            SlashItem.Actions.Move,
            SlashItem.Actions.MoveTo,
            SlashItem.Actions.CleanStyle
        )

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)
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

        val event = SlashEvent.Filter(filter = "/", viewType = Types.HOLDER_NUMBERED)

        vm.onSlashTextWatcherEvent(event = event)
        vm.onSlashItemClicked(SlashItem.Main.Alignment)

        val state = vm.controlPanelViewState.value

        val command = state?.slashWidget?.command as SlashCommand.UpdateItems

        assertNotNull(command)

        val expectedAlignmentItems = listOf(
            SlashItem.Subheader.AlignmentWithBack,
            SlashItem.Alignment.Left,
            SlashItem.Alignment.Center,
            SlashItem.Alignment.Right
        )

        val expected = SlashCommand.UpdateItems(
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
        assertEquals(expected = expected, actual = command)
    }
    //endregion

}