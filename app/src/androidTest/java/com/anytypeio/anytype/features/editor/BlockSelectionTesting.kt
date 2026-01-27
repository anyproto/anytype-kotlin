package com.anytypeio.anytype.features.editor

import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.features.editor.base.EditorTestSetup
import com.anytypeio.anytype.features.editor.base.TestEditorFragment
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsNotSelected
import com.anytypeio.anytype.test_utils.utils.checkIsSelected
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
class BlockSelectionTesting : EditorTestSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private val args = bundleOf(
        EditorFragment.CTX_KEY to root,
        EditorFragment.SPACE_ID_KEY to defaultSpace
    )

    private val title = MockBlockFactory.text(
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE,
            text = "Selection mode testing",
            marks = emptyList()
        )
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
    fun shouldSelectSingleBlockOnLongPress() {

        // SETUP

        val paragraph = MockBlockFactory.paragraph(text = "First paragraph block")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraph.id)
        )

        val document = listOf(page, header, title, paragraph)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        rvMatcher.onItemView(1, R.id.textContent).checkHasText("First paragraph block")
        rvMatcher.onItemView(1, R.id.selectionView).checkIsNotSelected()

        // Enter selection mode via long press
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraph.id)
            )
        }

        Thread.sleep(300)

        // Verify the block is selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldSelectThreeBlocksInSelectionMode() {

        // SETUP

        val paragraphA = MockBlockFactory.paragraph(text = "Paragraph A")
        val paragraphB = MockBlockFactory.paragraph(text = "Paragraph B")
        val paragraphC = MockBlockFactory.paragraph(text = "Paragraph C")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id, paragraphC.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB, paragraphC)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Verify initial state
        rvMatcher.onItemView(1, R.id.textContent).checkHasText("Paragraph A")
        rvMatcher.onItemView(2, R.id.textContent).checkHasText("Paragraph B")
        rvMatcher.onItemView(3, R.id.textContent).checkHasText("Paragraph C")

        rvMatcher.onItemView(1, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsNotSelected()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        // Verify: A selected, B and C not
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B to select it
        rvMatcher.onItemView(2, R.id.textContent).perform(ViewActions.click())

        Thread.sleep(100)

        // Verify: A and B selected, C not
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsNotSelected()

        // Click paragraph C to select it
        rvMatcher.onItemView(3, R.id.textContent).perform(ViewActions.click())

        Thread.sleep(100)

        // Verify: all three selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldDeselectBlockWhenClickedAgainInSelectionMode() {

        // SETUP

        val paragraphA = MockBlockFactory.paragraph(text = "Paragraph A")
        val paragraphB = MockBlockFactory.paragraph(text = "Paragraph B")
        val paragraphC = MockBlockFactory.paragraph(text = "Paragraph C")

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id, paragraphC.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB, paragraphC)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        // Select paragraphs B and C
        rvMatcher.onItemView(2, R.id.textContent).perform(ViewActions.click())
        Thread.sleep(100)
        rvMatcher.onItemView(3, R.id.textContent).perform(ViewActions.click())
        Thread.sleep(100)

        // Verify all three selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

        // Click paragraph B again to deselect it
        rvMatcher.onItemView(2, R.id.textContent).perform(ViewActions.click())
        Thread.sleep(100)

        // Verify: A and C still selected, B deselected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region MENTION SELECTION TESTS

    @Test
    fun shouldToggleSelectionWhenClickingBeforeMention() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        // Text: "Start Alice end"
        // Indices: "Start " = 0-5, "Alice" = 6-10, " end" = 11-14
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Start Alice end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 11),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        // Verify: A selected, B not selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at text offset 2 (inside "Start", before mention)
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(2))

        Thread.sleep(300)

        // Verify: B is now selected (click before mention toggles selection)
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldToggleSelectionWhenClickingAfterMention() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Start Alice end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 11),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        // Verify: A selected, B not selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at text offset 13 (inside "end", after mention)
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(13))

        Thread.sleep(300)

        // Verify: B is now selected (click after mention toggles selection)
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldToggleSelectionWhenClickingOnMention() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Start Alice end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 11),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        // Verify: A selected, B not selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at text offset 8 (inside "Alice", ON the mention)
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(8))

        Thread.sleep(300)

        // In selection mode (READ mode), enableReadMode() converts Editable to SpannedString,
        // making editableText null. EditorTouchProcessor cannot detect ClickableSpans,
        // so the click falls through and toggles selection — same as clicking plain text.
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldHandleSelectionToggleSequenceWithMentionBlock() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        // Text: "Start Alice end"
        // "Start " = 0-5, "Alice" = 6-10, " end" = 11-14
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Start Alice end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 11),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Step 1: Long click on block B → enters selection mode, B is selected
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphB.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Step 2: Click block A → A becomes selected, B stays selected
        rvMatcher.onItemView(1, R.id.textContent).perform(ViewActions.click())

        Thread.sleep(100)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Step 3: Click block B (regular click) → B deselected
        rvMatcher.onItemView(2, R.id.textContent).perform(ViewActions.click())

        Thread.sleep(100)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Step 4: Click block B (regular click) again → B selected
        rvMatcher.onItemView(2, R.id.textContent).perform(ViewActions.click())

        Thread.sleep(100)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Step 5: Click on mention in block B (offset 8, inside "Alice")
        // In read mode, ClickableSpan detection is skipped. The click toggles
        // block B's selection state: selected → not selected.
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(8))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Step 6: Click "end" on block B (offset 13, after the mention)
        // Click on plain text toggles selection: not selected → selected.
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(13))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion

    //region LINK SELECTION TESTS

    @Test
    fun shouldToggleSelectionWhenClickingOnUrlLink() {

        // SETUP

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        // Text: "Visit example site"
        // "Visit " = 0-5, "example" = 6-12, " site" = 13-17
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Visit example site",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 13),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://example.com"
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on paragraph A
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at offset 9 (inside "example", on the URL link)
        // In selection mode, ClickableSpan detection is skipped, so the click
        // toggles selection instead of opening the browser.
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(9))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Release pending coroutines
        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldToggleSelectionWhenClickingBeforeUrlLink() {

        // SETUP

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Visit example site",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 13),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://example.com"
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at offset 2 (inside "Visit", before the link)
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(2))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion

    //region OBJECT LINK SELECTION TESTS

    @Test
    fun shouldToggleSelectionWhenClickingOnObjectLink() {

        // SETUP

        val objectTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        // Text: "Open my page here"
        // "Open " = 0-4, "my page" = 5-11, " here" = 12-16
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Open my page here",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(5, 12),
                        type = Block.Content.Text.Mark.Type.OBJECT,
                        param = objectTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at offset 8 (inside "my page", on the object link)
        // In selection mode, ClickableSpan detection is skipped, so the click
        // toggles selection instead of navigating to the linked object.
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(8))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldToggleSelectionWhenClickingAfterObjectLink() {

        // SETUP

        val objectTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Open my page here",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(5, 12),
                        type = Block.Content.Text.Mark.Type.OBJECT,
                        param = objectTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click paragraph B at offset 15 (inside "here", after the object link)
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(15))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //endregion

    //region MIXED MARKUP SELECTION TESTS

    @Test
    fun shouldToggleSelectionWithMultipleClickableMarks() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()

        val paragraphA = MockBlockFactory.paragraph(text = "Plain paragraph")

        // Text: "See link and Alice end"
        // "See " = 0-3, "link" = 4-7, " and " = 8-12, "Alice" = 13-17, " end" = 18-21
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "See link and Alice end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(4, 8),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://example.com"
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(13, 18),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click on the LINK area (offset 6, inside "link") → B selected
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(6))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Click on the LINK area again (offset 6) → B deselected
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(6))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()

        // Click on the MENTION area (offset 15, inside "Alice") → B selected
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(15))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        advance(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    @Test
    fun shouldToggleSelectionAcrossBlocksWithDifferentMarkups() {

        // SETUP

        val mentionTarget = MockDataFactory.randomUuid()
        val objectTarget = MockDataFactory.randomUuid()

        // Block A: text with LINK mark
        // "Click link now" — "link" at range 6..10
        val paragraphA = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Click link now",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(6, 10),
                        type = Block.Content.Text.Mark.Type.LINK,
                        param = "https://example.com"
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        // Block B: text with MENTION mark
        // "Ask Alice please" — "Alice" at range 4..9
        val paragraphB = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Ask Alice please",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(4, 9),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        // Block C: text with OBJECT mark
        // "Open page end" — "page" at range 5..9
        val paragraphC = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = "Open page end",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(5, 9),
                        type = Block.Content.Text.Mark.Type.OBJECT,
                        param = objectTarget
                    )
                ),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, paragraphA.id, paragraphB.id, paragraphC.id)
        )

        val document = listOf(page, header, title, paragraphA, paragraphB, paragraphC)

        val details = ObjectViewDetails(
            mapOf(mentionTarget to mapOf(Block.Fields.NAME_KEY to "Alice"))
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(document, details)
        stubUpdateText()
        stubAnalytics()

        val scenario = launchFragment(args)

        // TESTING

        val rvMatcher = R.id.recycler.rVMatcher()

        // Enter selection mode via long press on block A (has LINK)
        scenario.onFragment { fragment ->
            fragment.viewModel.onClickListener(
                ListenerType.LongClick(target = paragraphA.id)
            )
        }

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsNotSelected()

        // Click block B at offset 6 (on mention "Alice") → B selected
        rvMatcher.onItemView(2, R.id.textContent).perform(ClickAtTextOffset(6))

        Thread.sleep(300)

        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()

        // Click block C at offset 7 (on object link "page") → C selected
        rvMatcher.onItemView(3, R.id.textContent).perform(ClickAtTextOffset(7))

        Thread.sleep(300)

        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

        // Verify all three blocks selected
        rvMatcher.onItemView(1, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

        // Click block A at offset 8 (on link "link") → A deselected
        rvMatcher.onItemView(1, R.id.textContent).perform(ClickAtTextOffset(8))

        Thread.sleep(300)

        rvMatcher.onItemView(1, R.id.selectionView).checkIsNotSelected()
        rvMatcher.onItemView(2, R.id.selectionView).checkIsSelected()
        rvMatcher.onItemView(3, R.id.selectionView).checkIsSelected()

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

    /**
     * Custom ViewAction that clicks at a specific character offset within a TextView.
     * Calculates screen coordinates from the text layout and dispatches touch events.
     */
    private class ClickAtTextOffset(private val offset: Int) : ViewAction {
        override fun getConstraints() = ViewMatchers.isAssignableFrom(TextView::class.java)
        override fun getDescription() = "Click at text offset $offset"
        override fun perform(uiController: UiController, view: View) {
            val tv = view as TextView
            val layout = tv.layout
            val line = layout.getLineForOffset(offset)
            val x = layout.getPrimaryHorizontal(offset) + tv.totalPaddingLeft - tv.scrollX
            val y = ((layout.getLineTop(line) + layout.getLineBottom(line)) / 2f) +
                    tv.totalPaddingTop - tv.scrollY
            val downTime = SystemClock.uptimeMillis()
            val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
            val up = MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_UP, x, y, 0)
            view.dispatchTouchEvent(down)
            uiController.loopMainThreadForAtLeast(50)
            view.dispatchTouchEvent(up)
            down.recycle()
            up.recycle()
            uiController.loopMainThreadUntilIdle()
        }
    }
}
