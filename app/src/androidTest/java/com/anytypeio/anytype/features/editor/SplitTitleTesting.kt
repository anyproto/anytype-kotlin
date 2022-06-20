package com.anytypeio.anytype.features.editor

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsFocused
import com.anytypeio.anytype.test_utils.utils.checkIsNotFocused
import com.anytypeio.anytype.test_utils.utils.espresso.SetEditTextSelectionAction
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.utils.CoroutinesTestRule
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@LargeTest
class SplitTitleTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(EditorFragment.ID_KEY to root)

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldSetCursorAtTheEndOfDescription() {
        val title = Block(
            id = "title",
            content = StubTextContent(
                text = "Title " + "83O6sVya",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
        val featured = Block(
            id = Relations.FEATURED_RELATIONS,
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )
        val description = Block(
            id = Relations.DESCRIPTION,
            content = StubTextContent(
                text = "Description " + "6HJ36U",
                style = Block.Content.Text.Style.DESCRIPTION,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
        val header = Block(
            id = "header",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id, description.id, featured.id)
        )
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Text Block " + "D8T09",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val document = listOf(page, header, title, description, featured, block)

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(Relations.FEATURED_RELATIONS to listOf(description.id, relation2.key))
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf()
        )

        // TESTING

        val scenario = launchFragment(args = args)

        // TESTING

        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.title).perform(ViewActions.click())
            onItemView(0, R.id.title)
                .perform(
                    SetEditTextSelectionAction(
                        selection = title.content.asText().text.length
                    )
                )
            onItemView(0, R.id.title)
                .perform(ViewActions.pressImeActionButton())
        }

        Thread.sleep(300)

        scenario.onFragment(action = { fragment ->
            val item = fragment.binding.recycler.getChildAt(1)
            val view = item.findViewById<TextInputWidget>(R.id.tvBlockDescription)
            val descLength = description.content.asText().text.length
            assertEquals(expected = descLength, actual = view.selectionStart)
            assertEquals(expected = descLength, actual = view.selectionEnd)
        })

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

    }

    @Test
    fun shouldSetCursorAtTheStartOfDescriptionAfterTitleSplit() {
        val title = Block(
            id = "title",
            content = StubTextContent(
                text = "Title " + "83O6sVya",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
        val featured = Block(
            id = Relations.FEATURED_RELATIONS,
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )
        val description = Block(
            id = Relations.DESCRIPTION,
            content = StubTextContent(
                text = "Description " + "6HJ36U",
                style = Block.Content.Text.Style.DESCRIPTION,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )
        val header = Block(
            id = "header",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id, description.id, featured.id)
        )
        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Text Block " + "D8T09",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val document = listOf(page, header, title, description, featured, block)

        val customDetails = Block.Details(
            mapOf(
                root to Block.Fields(
                    mapOf(Relations.FEATURED_RELATIONS to listOf(description.id, relation2.key))
                )
            )
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubUpdateText()
        stubOpenDocument(
            document = document,
            details = customDetails,
            relations = listOf()
        )
        stubSplitBlocks(
            command = Command.Split(
                context = root,
                target = title.id,
                range = IntRange(6, 6),
                style = Block.Content.Text.Style.TITLE,
                mode = BlockSplitMode.BOTTOM
            ),
            new = description.id,
            events = listOf(
                Event.Command.GranularChange(
                    id = title.id,
                    text = "Title ",
                    context = root
                ),
                Event.Command.GranularChange(
                    id = description.id,
                    text = "83O6sVya Description 6HJ36U",
                    context = root
                ),
                Event.Command.Details.Amend(
                    context = root,
                    target = root,
                    details = mapOf(description.id to "83O6sVya Description 6HJ36U")
                ),
                Event.Command.Details.Amend(
                    context = root,
                    target = root,
                    details = mapOf("name" to "Title ")
                )
            )
        )

        // TESTING

        launchFragment(args = args)
        
        with(R.id.recycler.rVMatcher()) {
            onItemView(0, R.id.title).perform(ViewActions.click())
            onItemView(0, R.id.title)
                .perform(
                    SetEditTextSelectionAction(
                        selection = 6
                    )
                )
            onItemView(0, R.id.title)
                .perform(ViewActions.pressImeActionButton())
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        val command = Command.Split(
            context = root,
            target = title.id,
            style = Block.Content.Text.Style.TITLE,
            range = IntRange(6, 6),
            mode = BlockSplitMode.BOTTOM
        )

        verifyBlocking(repo, times(1)) { split(command) }

        with(R.id.recycler.rVMatcher()) {
            with(onItemView(0, R.id.title)) {
                checkIsDisplayed()
                checkIsNotFocused()
                checkHasText("Title ")
            }
            with(onItemView(1, R.id.tvBlockDescription)) {
                checkIsDisplayed()
                checkIsFocused()
                checkHasText("83O6sVya Description 6HJ36U")
            }
        }

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }

    private fun launchFragment(args: Bundle): FragmentScenario<TestEditorFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}