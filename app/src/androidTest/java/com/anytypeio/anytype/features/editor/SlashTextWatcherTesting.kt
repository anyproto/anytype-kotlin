package com.anytypeio.anytype.features.editor

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.action.ViewActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestPageFragment
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.ui.page.PageFragment
import com.anytypeio.anytype.utils.*
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class SlashTextWatcherTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(PageFragment.ID_KEY to root)

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

    @Test
    fun testShouldShowSlashWidgetOnSlashInEmptyText() {
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowSlashWidgetOnSlashAfterSpace() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SPACE))
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowSlashWidgetOnSlashAfterText() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowSlashWidgetOnSlashBeforeText() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent).perform(ViewActions.replaceText("Foo"))
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowSlashWidgetOnSlashAfterSlash() {
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldShowSlashWidgetOnInsertTextWithSlashAfterSlash() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo/",
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
        }

        R.id.slashWidget.matchView().checkIsDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldCloseMenuWhenCharSlashDeleted() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
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
        stubOpenDocument(document, defaultDetails)

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_DEL))
        }

        R.id.slashWidget.matchView().checkIsNotDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldCloseMenuWhenClickInBlock() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
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
        stubOpenDocument(document, defaultDetails)

        val scenario = launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(1, R.id.textContent).perform(ViewActions.click())
            onItemView(1, R.id.textContent)
                .perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
            Thread.sleep(300)

            R.id.slashWidget.matchView().checkIsDisplayed()

            onItemView(1, R.id.textContent).perform(ViewActions.click())
            Thread.sleep(300)
        }

        R.id.slashWidget.matchView().checkIsNotDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun testShouldCloseMenuWhenFocusIsChanged() {
        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val paragraph2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
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

        launchFragment(args)

        with(R.id.recycler.rVMatcher()) {
            onItemView(2, R.id.textContent).perform(ViewActions.click())
            onItemView(2, R.id.textContent).perform(ViewActions.pressKey(KeyEvent.KEYCODE_SLASH))
            Thread.sleep(300)

            R.id.slashWidget.matchView().checkIsDisplayed()

            onItemView(1, R.id.textContent).perform(ViewActions.click())
            Thread.sleep(300)
        }

        R.id.slashWidget.matchView().checkIsNotDisplayed()

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }


    // STUBBING & SETUP

    private fun launchFragment(args: Bundle): FragmentScenario<TestPageFragment> {
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