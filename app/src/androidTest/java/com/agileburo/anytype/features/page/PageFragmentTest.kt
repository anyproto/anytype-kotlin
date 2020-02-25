package com.agileburo.anytype.features.page

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.mocking.MockDataFactory
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_BULLET
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_CHECKBOX
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_CODE
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H2
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H3
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H4
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_NUMBERED_1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH_1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_QUOTE
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_TITLE
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_TOGGLE
import com.agileburo.anytype.presentation.page.DocumentExternalEventReducer
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.page.PageFragment
import com.agileburo.anytype.utils.CoroutinesTestRule
import com.agileburo.anytype.utils.TestUtils.withRecyclerView
import com.agileburo.anytype.utils.scrollTo
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
Helping link For Espresso RecyclerView actions:
https://github.com/android/testing-samples/blob/master/ui/espresso/RecyclerViewSample/app/src/androidTest/java/com/example/android/testing/espresso/RecyclerViewSample/RecyclerViewSampleTest.java
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class PageFragmentTest {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var openPage: OpenPage
    @Mock
    lateinit var closePage: ClosePage
    @Mock
    lateinit var updateBlock: UpdateBlock
    @Mock
    lateinit var createBlock: CreateBlock
    @Mock
    lateinit var interceptEvents: InterceptEvents
    @Mock
    lateinit var updateCheckbox: UpdateCheckbox
    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks
    @Mock
    lateinit var duplicateBlock: DuplicateBlock
    @Mock
    lateinit var updateTextStyle: UpdateTextStyle
    @Mock
    lateinit var updateTextColor: UpdateTextColor
    @Mock
    lateinit var updateLinkMarks: UpdateLinkMarks
    @Mock
    lateinit var removeLinkMark: RemoveLinkMark
    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var splitBlock: SplitBlock
    private lateinit var createPage: CreatePage
    private lateinit var updateBackgroundColor: UpdateBackgroundColor

    private lateinit var actionToolbar: ViewInteraction
    private lateinit var optionToolbar: ViewInteraction

    private val root: String = "rootId123"

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        actionToolbar = onView(withId(R.id.actionToolbar))
        optionToolbar = onView(withId(R.id.optionToolbar))

        splitBlock = SplitBlock(repo)
        createPage = CreatePage(repo)
        updateBackgroundColor = UpdateBackgroundColor(repo)

        TestPageFragment.testViewModelFactory = PageViewModelFactory(
            openPage = openPage,
            closePage = closePage,
            updateBlock = updateBlock,
            createBlock = createBlock,
            interceptEvents = interceptEvents,
            updateCheckbox = updateCheckbox,
            unlinkBlocks = unlinkBlocks,
            duplicateBlock = duplicateBlock,
            updateTextStyle = updateTextStyle,
            updateTextColor = updateTextColor,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            mergeBlocks = mergeBlocks,
            splitBlock = splitBlock,
            createPage = createPage,
            documentEventReducer = DocumentExternalEventReducer(),
            updateBackgroundColor = updateBackgroundColor
        )
    }

    @Test
    fun shouldHaveTextSetForTextBlocks() {

        // SETUP

        val delayBeforeGettingEvents = 100L

        val args = bundleOf(PageFragment.ID_KEY to root)

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(
                    BLOCK_H1.id, BLOCK_H2.id, BLOCK_H3.id,
                    BLOCK_H4.id, BLOCK_TITLE.id, BLOCK_PARAGRAPH.id,
                    BLOCK_QUOTE.id, BLOCK_CODE.id, BLOCK_BULLET.id,
                    BLOCK_NUMBERED_1.id, BLOCK_TOGGLE.id, BLOCK_CHECKBOX.id
                )
            ),
            BLOCK_H1, BLOCK_H2, BLOCK_H3, BLOCK_H4, BLOCK_TITLE,
            BLOCK_PARAGRAPH, BLOCK_QUOTE, BLOCK_CODE, BLOCK_BULLET,
            BLOCK_NUMBERED_1, BLOCK_TOGGLE, BLOCK_CHECKBOX
        )

        stubShowBlock(
            initialDelay = delayBeforeGettingEvents,
            blocks = page
        )

        launchFragment(args)

        advance(delayBeforeGettingEvents)

        // TESTING

        onView(withId(R.id.recycler)).check(matches(isDisplayed()))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.headerOne))
            .check(matches(withText(BLOCK_H1.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.headerTwo))
            .check(matches(withText(BLOCK_H2.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.headerThree))
            .check(matches(withText(BLOCK_H3.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(3, R.id.headerThree))
            .check(matches(withText(BLOCK_H4.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(4, R.id.title))
            .check(matches(withText(BLOCK_TITLE.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(5, R.id.textContent))
            .check(matches(withText(BLOCK_PARAGRAPH.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(6, R.id.highlightContent))
            .check(matches(withText(BLOCK_QUOTE.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(7, R.id.snippet))
            .check(matches(withText(BLOCK_CODE.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(8, R.id.bulletedListContent))
            .check(matches(withText(BLOCK_BULLET.content.asText().text)))

        R.id.recycler.scrollTo<BlockViewHolder.Numbered>(9)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(9, R.id.numberedListContent))
            .check(matches(withText(BLOCK_NUMBERED_1.content.asText().text)))

        onView(
            withRecyclerView(R.id.recycler).atPositionOnView(9, R.id.number)
        ).check(matches(withText("1")))

        R.id.recycler.scrollTo<BlockViewHolder.Toggle>(10)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(10, R.id.toggleContent))
            .check(matches(withText(BLOCK_TOGGLE.content.asText().text)))

        R.id.recycler.scrollTo<BlockViewHolder.Checkbox>(11)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(11, R.id.checkboxContent))
            .check(matches(withText(BLOCK_CHECKBOX.content.asText().text)))
    }

    @Test
    fun shouldAppendTextToTheEndAfterTyping() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(
                    BLOCK_PARAGRAPH_1.id
                )
            ),
            BLOCK_PARAGRAPH_1
        )

        stubShowBlock(
            initialDelay = delayBeforeGettingEvents,
            blocks = page
        )

        launchFragment(args)

        advance(delayBeforeGettingEvents)

        // TESTING

        val text = " Add new text at the end"

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        target.apply {
            perform(click())
            perform(typeText(text))
            perform(closeSoftKeyboard())
        }

        advance(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        val expected = BLOCK_PARAGRAPH_1.content.asText().text + text

        target.check(matches(withText(expected)))
    }

    @Test
    fun shouldShowBlockToolbarAsSoonAsTitleIsFocused() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L

        val title = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(title.id)
            ),
            title
        )

        stubShowBlock(
            initialDelay = delayBeforeGettingEvents,
            blocks = page
        )

        launchFragment(args)

        advance(delayBeforeGettingEvents)

        // TESTING

        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.title))

        target.apply { perform(click()) }

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldClearFocusAfterClickedOnHideKeyboard() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L

        val title = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(title.id)
            ),
            title
        )

        stubShowBlock(
            initialDelay = delayBeforeGettingEvents,
            blocks = page
        )

        launchFragment(args)

        advance(delayBeforeGettingEvents)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.title))

        target.perform(click())

        onView(allOf(withId(R.id.keyboard), isDisplayed())).perform(click())
        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))
        target.check(matches(not(hasFocus())))
    }

    @Test
    fun shouldSplitBlocks() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeSplittingBlocks = 100L

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "FooBar",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        val new = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Bar",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(delayBeforeSplittingBlocks)
                emit(
                    listOf(
                        Event.Command.GranularChange(
                            context = root,
                            id = paragraph.id,
                            text = "Foo"
                        ),
                        Event.Command.UpdateStructure(
                            context = root,
                            id = page.first().id,
                            children = listOf(paragraph.id, new.id)
                        ),
                        Event.Command.AddBlock(
                            context = root,
                            blocks = listOf(new)
                        )
                    )
                )
            }
        )

        launchFragment(args)

        // TESTING

        advance(delayBeforeGettingEvents)

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        target.check(matches(withText(paragraph.content.asText().text)))

        advance(delayBeforeSplittingBlocks)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("Foo")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)).apply {
            check(matches(withText("Bar")))
        }
    }

    @Test
    fun shouldCreateDividerBlockAfterFirstBlock() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeAddingDivider = 100L

        val paragraphBefore = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Block before divider",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val paragraphAfter = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Block after divider",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraphBefore.id, paragraphAfter.id)
            ),
            paragraphBefore,
            paragraphAfter
        )

        val divider = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Divider
        )

        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(delayBeforeAddingDivider)
                emit(
                    listOf(
                        Event.Command.GranularChange(
                            context = root,
                            id = paragraphBefore.id,
                            text = "Block before divider, get focus and add divider"
                        ),
                        Event.Command.UpdateStructure(
                            context = root,
                            id = page.first().id,
                            children = listOf(paragraphBefore.id, divider.id, paragraphAfter.id)
                        ),
                        Event.Command.AddBlock(
                            context = root,
                            blocks = listOf(divider)
                        )
                    )
                )
            }
        )

        launchFragment(args)

        // TESTING

        advance(delayBeforeGettingEvents)

        val target1 = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))
        val target2 = onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent))

        target1.check(matches(withText(paragraphBefore.content.asText().text)))
        target2.check(matches(withText(paragraphAfter.content.asText().text)))

        advance(delayBeforeAddingDivider)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("Block before divider, get focus and add divider")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.divider)).apply {
            check(matches(isDisplayed()))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.textContent)).apply {
            check(matches(withText("Block after divider")))
        }
    }

    @Test
    fun shouldCreateNewEmptyParagraph() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeAddingNewBlock = 100L

        val paragraph1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "First block",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val paragraph2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Second block",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph1.id, paragraph2.id)
            ),
            paragraph1,
            paragraph2
        )

        val paragraphNew = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(delayBeforeAddingNewBlock)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = page.first().id,
                            children = listOf(paragraph1.id, paragraphNew.id, paragraph2.id)
                        ),
                        Event.Command.AddBlock(
                            context = root,
                            blocks = listOf(paragraphNew)
                        )
                    )
                )
            }
        )

        launchFragment(args)

        //TESTING

        advance(delayBeforeGettingEvents)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            perform(click())
        }

        advance(delayBeforeAddingNewBlock)

        Thread.sleep(1500)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent)).apply {
            check(matches(withText("First block")))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent)).apply {
            check(matches(withText("")))
            check(matches(isDisplayed()))
        }
        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.textContent)).apply {
            check(matches(withText("Second block")))
        }
    }

    /*
    @Test
    fun shouldHideOptionToolbarsOnEmptyBlockClick() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val delayBeforeGettingEvents = 100L
        val delayBeforeKeyboardIsHidden = 300L

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

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(paragraph.id)
            ),
            paragraph
        )

        stubEvents(
            events = flow {
                delay(delayBeforeGettingEvents)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
            }
        )

        launchFragment(args)

        // TESTING

        advance(delayBeforeGettingEvents)

        val targetBlock = onView(withRecyclerView(R.id.recycler).atPositionOnView(0, R.id.textContent))

        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))

        targetBlock.perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(allOf(withId(R.id.actions))).perform(click())

        advance(delayBeforeKeyboardIsHidden)

        val actionToolbar = onView(withId(R.id.actionToolbar))

        actionToolbar.check(matches(isDisplayed()))

        targetBlock.apply { perform(click()) }

        actionToolbar.check(matches(not(isDisplayed())))
    }
     */

    private fun launchFragment(args: Bundle) {
        launchFragmentInContainer<TestPageFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }

    /**
     * STUBBING
     */

    private fun stubShowBlock(initialDelay: Long, blocks: List<Block>) {
        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(initialDelay)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = blocks,
                            context = root
                        )
                    )
                )
            }
        }
    }

    private fun stubEvents(events: Flow<List<Event>>) {
        interceptEvents.stub {
            onBlocking { build() } doReturn events
        }
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}