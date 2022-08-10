package com.anytypeio.anytype.features.editor

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubBookmark
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.MainMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.MediaMenuHolder
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.MockBlockFactory.paragraph
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.espresso.SetEditTextSelectionAction
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.performClick
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SlashWidgetTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(EditorFragment.ID_KEY to root)

    private val defaultDetails = Block.Details(
        mapOf(
            root to Block.Fields(
                mapOf(
                    "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
                )
            )
        )
    )

    private val title = Block(
        id = MockDataFactory.randomUuid(),
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE,
            text = "SlashTextWatcherTesting",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    override fun setup() {
        super.setup()
    }


    /**
     * Slash widget, all cases (SH - some sub header, STYLE - all style + markup items)
     * Tests:
     * 1. empty block | show MAIN items, BACK invisible | +
     * 2. not empty block | show MAIN items, BACK invisible | +
     * 3. show MAIN items | click STYLE | show SH + STYLE items,  BACK visible | +
     * 4. show MAIN items | click STYLE | show SH + STYLE items | BACK clicked | show MAIN items | +
     * 5. show MAIN items | click MEDIA | show SH + MEDIA items,  BACK visible | +
     * 6. show MAIN items | click MEDIA | BACK clicked | show MAIN items | +
     * 7. show MAIN items | click RELATIONS | show SH + RELATIONS, BACK visible | +
     * 8. show MAIN items | click OBJECTS | show SH + OBJECT TYPES, BACK visible | +
     * 9. show MAIN items | click MEDIA | click FILE | no focus, slash widget is invisible, add file block | +
     * 10.show MAIN items | click MEDIA | click PICTURE | no focus, slash widget is invisible, add picture block | +
     * 11.show MAIN items | click MEDIA | click VIDEO | no focus, slash widget is invisible, add video block | +
     * 12.show MAIN items | click MEDIA | click BOOKMARK | no focus, slash widget is invisible, add bookmark block | +
     * 13.show MAIN items | click OTHERS | show SH + OTHERS items, BACK visible | +
     * 14 click on SLASH BUTTON | add slash char to block
     * 15 click on SLASH BUTTON | show MAIN items
     */

    //region {Test 1}
    @Test
    fun testShouldShowMainItems() {
        val paragraph = paragraph(text = "")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.textMain).checkHasText(R.string.slash_widget_main_media)
            onItemView(2, R.id.textMain).checkHasText(R.string.slash_widget_main_objects)

            checkIsRecyclerSize(9)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 2}
    @Test
    fun testShouldAlsoShowMainItems() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.textMain).checkHasText(R.string.slash_widget_main_media)
            onItemView(2, R.id.textMain).checkHasText(R.string.slash_widget_main_objects)

            checkIsRecyclerSize(9)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 3}
    @Test
    fun testShouldShowStyleItems() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).performClick()

            onItemView(0, R.id.subheader).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.tvTitle).checkHasText(R.string.slash_widget_style_text)
            onItemView(2, R.id.tvTitle).checkHasText(R.string.slash_widget_style_title)
            onItemView(3, R.id.tvTitle).checkHasText(R.string.slash_widget_style_heading)

            checkIsRecyclerSize(15)
        }

        onView(withId(R.id.flBack)).checkIsDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 4}
    @Test
    fun testShouldNavigateFromStyleToMain() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).performClick()
            onItemView(0, R.id.flBack).performClick()
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.textMain).checkHasText(R.string.slash_widget_main_media)
            onItemView(2, R.id.textMain).checkHasText(R.string.slash_widget_main_objects)

            checkIsRecyclerSize(9)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 5}
    @Test
    fun testShouldShowMediaItems() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()

            onItemView(0, R.id.subheader).checkHasText(R.string.slash_widget_main_media)
            onItemView(1, R.id.tvTitle).checkHasText(R.string.slash_widget_media_file)
            onItemView(2, R.id.tvTitle).checkHasText(R.string.slash_widget_media_picture)
            onItemView(3, R.id.tvTitle).checkHasText(R.string.slash_widget_media_video)

            checkIsRecyclerSize(6)
        }

        onView(withId(R.id.flBack)).checkIsDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 6}
    @Test
    fun testShouldNavigateFromMediaToMain() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()
            onItemView(0, R.id.flBack).performClick()
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.textMain).checkHasText(R.string.slash_widget_main_media)
            onItemView(2, R.id.textMain).checkHasText(R.string.slash_widget_main_objects)

            checkIsRecyclerSize(9)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 7}
    @Test
    fun testShouldShowRelations() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val relation1 = Relation(
            key = MockDataFactory.randomString(),
            name = "Name",
            format = Relation.Format.SHORT_TEXT,
            source = Relation.Source.DETAILS
        )

        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Number",
            format = Relation.Format.NUMBER,
            source = Relation.Source.DETAILS
        )
        val relation1Value = "Earth"
        val relation2Value = 16196.04

        val relations = listOf(relation1, relation2)

        val details = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(
                        "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
                        relation1.key to relation1Value,
                        relation2.key to relation2Value
                    )
                )
            )
        )
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubGetObjectTypes(objectTypes = emptyList())
        stubOpenDocument(document, details, relations)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        onView(withId(R.id.rvSlash)).perform(RecyclerViewActions.scrollToPosition<MainMenuHolder>(3))

        Thread.sleep(3000)

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(3, R.id.textMain).performClick()

            Thread.sleep(3000)

            onItemView(2, R.id.tvRelationTitle).checkHasText(relation1.name)
            onItemView(2, R.id.tvRelationValue).checkHasText(relation1Value)
            onItemView(3, R.id.tvRelationTitle).checkHasText(relation2.name)

            val numValue = NumberParser.parse(relation2Value)
            checkNotNull(numValue)

            onItemView(3, R.id.tvRelationValue).checkHasText(numValue)

            checkIsRecyclerSize(4)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 8}
    @Test
    fun testShouldShowObjectTypes() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        val objectTypes = listOf(
            ObjectType(
                url = MockDataFactory.randomUuid(),
                name = MockDataFactory.randomString(),
                emoji = MockDataFactory.randomString(),
                layout = ObjectType.Layout.BASIC,
                relations = emptyList(),
                description = MockDataFactory.randomString(),
                isHidden = false,
                smartBlockTypes = listOf(),
                isArchived = false,
                isReadOnly = false
            ),
            ObjectType(
                url = MockDataFactory.randomUuid(),
                name = MockDataFactory.randomString(),
                emoji = MockDataFactory.randomString(),
                layout = ObjectType.Layout.BASIC,
                relations = emptyList(),
                description = MockDataFactory.randomString(),
                isHidden = false,
                smartBlockTypes = listOf(),
                isArchived = false,
                isReadOnly = false
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)
        stubGetObjectTypes(objectTypes)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        //TESTING

        onView(withId(R.id.rvSlash)).perform(RecyclerViewActions.scrollToPosition<MainMenuHolder>(2))

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(2, R.id.textMain).performClick()
            onItemView(0, R.id.subheader).checkHasText(R.string.slash_widget_main_objects_subheader)
            onItemView(0, R.id.flBack).checkIsDisplayed()
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

    }
    //endregion

    //region {Test 9}
    @Test
    fun shouldCreateFileBlockBelowSlash() {
        val paragraph = paragraph(text = "FooBar")
        val paragraph2 = paragraph(text = "Second")

        val file = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.FILE,
                state = Block.Content.File.State.EMPTY
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, paragraph2.id)
        )

        val document = listOf(page, header, title, paragraph, paragraph2)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = paragraph.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.File(
                    type = Block.Content.File.Type.FILE,
                    state = Block.Content.File.State.EMPTY
                )
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, paragraph.id, file.id, paragraph2.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(file)
                )
            )
        )

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()
            onItemView(1, R.id.tvTitle).performClick()
        }

        //TESTING

        onView(withId(R.id.slashWidget)).checkIsNotDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 10}
    @Test
    fun shouldCreatePictureBlockBelowSlash() {
        val paragraph = paragraph(text = "FooBar")
        val paragraph2 = paragraph(text = "Second")

        val picture = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.EMPTY
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, paragraph2.id)
        )

        val document = listOf(page, header, title, paragraph, paragraph2)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = paragraph.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.File(
                    type = Block.Content.File.Type.IMAGE,
                    state = Block.Content.File.State.EMPTY
                )
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, paragraph.id, picture.id, paragraph2.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(picture)
                )
            )
        )

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()
        }

        onView(withId(R.id.rvSlash)).perform(RecyclerViewActions.scrollToPosition<MediaMenuHolder>(2))

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(2, R.id.tvTitle).performClick()
        }

        //TESTING

        onView(withId(R.id.slashWidget)).checkIsNotDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 11}
    @Test
    fun shouldCreateVideoBlockBelowSlash() {
        val paragraph = paragraph(text = "FooBar")

        val paragraph2 = paragraph(text = "Second")

        val video = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                type = Block.Content.File.Type.VIDEO,
                state = Block.Content.File.State.EMPTY
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, paragraph2.id)
        )

        val document = listOf(page, header, title, paragraph, paragraph2)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = paragraph.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.File(
                    type = Block.Content.File.Type.VIDEO,
                    state = Block.Content.File.State.EMPTY
                )
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, paragraph.id, video.id, paragraph2.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(video)
                )
            )
        )

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()
        }

        onView(withId(R.id.rvSlash)).perform(RecyclerViewActions.scrollToPosition<MediaMenuHolder>(3))

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(3, R.id.tvTitle).performClick()
        }

        //TESTING

        onView(withId(R.id.slashWidget)).checkIsNotDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 12}
    @Test
    fun shouldCreateBookmarkBlockBelowSlash() {
        val paragraph = paragraph(text = "FooBar")
        val paragraph2 = paragraph(text = "Second")

        val bookmark = StubBookmark()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id, paragraph2.id)
        )

        val document = listOf(page, header, title, paragraph, paragraph2)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)
        stubCreateBlock(
            params = CreateBlock.Params(
                context = root,
                target = paragraph.id,
                position = Position.BOTTOM,
                prototype = Block.Prototype.Bookmark
            ),
            events = listOf(
                Event.Command.UpdateStructure(
                    context = root,
                    id = root,
                    children = listOf(header.id, paragraph.id, bookmark.id, paragraph2.id)
                ),
                Event.Command.AddBlock(
                    context = root,
                    blocks = listOf(bookmark)
                )
            )
        )

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(1, R.id.textMain).performClick()
        }

        onView(withId(R.id.rvSlash)).perform(RecyclerViewActions.scrollToPosition<MediaMenuHolder>(4))

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(4, R.id.tvTitle).performClick()
        }

        //TESTING

        onView(withId(R.id.slashWidget)).checkIsNotDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 13}
    @Test
    fun shouldShowOtherItems() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        onView(withId(R.id.rvSlash))
            .perform(RecyclerViewActions.scrollToPosition<MediaMenuHolder>(4))

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(4, R.id.textMain).performClick()
        }

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.subheader).checkHasText(R.string.slash_widget_main_other)
            onItemView(1, R.id.tvTitle).checkHasText(R.string.slash_widget_other_line)
            onItemView(2, R.id.tvTitle).checkHasText(R.string.slash_widget_other_dots)
        }

        onView(withId(R.id.flBack)).checkIsDisplayed()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 14}
    @Test
    fun shouldAddSlashToTextOnSlashButtonClicked() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
        }

        onView(withId(R.id.slashWidgetButton)).performClick()

        //TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).checkHasText("FooBar/")
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    //region {Test 15}
    @Test
    fun shouldShowSlashWidgetOnSlashButtonClicked() {
        val paragraph = paragraph(text = "FooBar")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent).perform(SetEditTextSelectionAction(selection = 3))

        }

        onView(withId(R.id.slashWidgetButton)).performClick()

        //TESTING

        with(R.id.rvSlash.rVMatcher()) {
            onItemView(0, R.id.textMain).checkHasText(R.string.slash_widget_main_style)
            onItemView(1, R.id.textMain).checkHasText(R.string.slash_widget_main_media)
            onItemView(2, R.id.textMain).checkHasText(R.string.slash_widget_main_objects)

            checkIsRecyclerSize(9)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
    //endregion

    // STUBBING & SETUP

    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}