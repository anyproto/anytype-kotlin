package com.anytypeio.anytype.features.editor.base

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.action.ViewActions.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasChildViewWithText
import com.anytypeio.anytype.test_utils.utils.checkIsDisplayed
import com.anytypeio.anytype.test_utils.utils.checkIsNotDisplayed
import com.anytypeio.anytype.test_utils.utils.espresso.SetEditTextSelectionAction
import com.anytypeio.anytype.test_utils.utils.matchView
import com.anytypeio.anytype.test_utils.utils.onItemView
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
class MentionWidgetTesting : EditorTestSetup() {

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
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "MentionTextWatcherTesting",
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

    @Test
    fun testShouldShowMentionWidgetOnIconClickedAndSelectionStart() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

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
        stubGetListPages(listOf())
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(click())
        }

        R.id.blockMentionButton.matchView().perform(click())

        Thread.sleep(200)

        //TESTING

        R.id.mentionSuggesterToolbar.matchView().apply {
            checkIsDisplayed()
        }

        R.id.recycler.matchView().apply {
            checkHasChildViewWithText(1, "@", R.id.textContent)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowMentionWidgetOnIconClickedAndSelectionAfterSpace() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "test ",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

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
        stubGetListPages(listOf())
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(click())
            onItemView(1, R.id.textContent).perform(SetEditTextSelectionAction(selection = 5))
        }

        R.id.blockMentionButton.matchView().perform(click())

        Thread.sleep(200)

        //TESTING

        R.id.mentionSuggesterToolbar.matchView().apply {
            checkIsDisplayed()
        }

        R.id.recycler.matchView().apply {
            checkHasChildViewWithText(1, "test @", R.id.textContent)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldNotShowMentionWidgetOnIconClickedAndSelectionAfterNotSpace() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "test ",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

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
        stubGetListPages(listOf())
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(click())
            onItemView(1, R.id.textContent).perform(SetEditTextSelectionAction(selection = 4))
        }

        R.id.blockMentionButton.matchView().perform(click())

        Thread.sleep(200)

        //TESTING

        R.id.mentionSuggesterToolbar.matchView().apply {
            checkIsNotDisplayed()
        }

        R.id.recycler.matchView().apply {
            checkHasChildViewWithText(1, "test@ ", R.id.textContent)
        }

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

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