package com.agileburo.anytype.features.page

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.pattern.DefaultPatternMatcher
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.clipboard.Clipboard
import com.agileburo.anytype.domain.clipboard.Copy
import com.agileburo.anytype.domain.clipboard.Paste
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.download.DownloadFile
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.domain.page.bookmark.SetupBookmark
import com.agileburo.anytype.mocking.MockDataFactory
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_BULLET
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_CHECKBOX
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H2
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_H3
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_HIGHLIGHT
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_NUMBERED_1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_PARAGRAPH_1
import com.agileburo.anytype.mocking.MockUiTests.BLOCK_TOGGLE
import com.agileburo.anytype.presentation.page.DocumentExternalEventReducer
import com.agileburo.anytype.presentation.page.Editor
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.presentation.page.editor.Interactor
import com.agileburo.anytype.presentation.page.editor.Orchestrator
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import com.agileburo.anytype.ui.page.PageFragment
import com.agileburo.anytype.utils.CoroutinesTestRule
import com.agileburo.anytype.utils.TestUtils.withRecyclerView
import com.agileburo.anytype.utils.scrollTo
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
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
class EditorIntegrationTesting {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    private lateinit var archiveDocument: ArchiveDocument
    private lateinit var createDocument: CreateDocument
    private lateinit var downloadFile: DownloadFile
    private lateinit var undo: Undo
    private lateinit var redo: Redo
    private lateinit var copy: Copy
    private lateinit var paste: Paste
    private lateinit var updateTitle: UpdateTitle
    private lateinit var updateAlignment: UpdateAlignment
    private lateinit var replaceBlock: ReplaceBlock
    private lateinit var setupBookmark: SetupBookmark
    private lateinit var uploadUrl: UploadUrl
    private lateinit var splitBlock: SplitBlock
    private lateinit var createPage: CreatePage
    private lateinit var updateBackgroundColor: UpdateBackgroundColor

    @Mock
    lateinit var openPage: OpenPage
    @Mock
    lateinit var closePage: ClosePage
    @Mock
    lateinit var updateText: UpdateText
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
    lateinit var uriMatcher: Clipboard.UriMatcher
    @Mock
    lateinit var repo: BlockRepository
    @Mock
    lateinit var clipboard: Clipboard

    private val root: String = "rootId123"

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomString(),
        profile = MockDataFactory.randomUuid()
    )

    private val urlBuilder = UrlBuilder(
        config = config
    )

    private val stores = Editor.Storage()

    private val proxies = Editor.Proxer()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        splitBlock = SplitBlock(repo)
        createPage = CreatePage(repo)
        archiveDocument = ArchiveDocument(repo)
        createDocument = CreateDocument(repo)
        undo = Undo(repo)
        redo = Redo(repo)
        replaceBlock = ReplaceBlock(repo)
        setupBookmark = SetupBookmark(repo)
        updateAlignment = UpdateAlignment(repo)
        updateTitle = UpdateTitle(repo)
        uploadUrl = UploadUrl(repo)
        downloadFile = DownloadFile(
            downloader = mock(),
            context = Dispatchers.Main
        )
        copy = Copy(
            repo = repo,
            clipboard = clipboard
        )

        paste = Paste(
            repo = repo,
            clipboard = clipboard,
            matcher = uriMatcher
        )

        updateBackgroundColor = UpdateBackgroundColor(repo)

        TestPageFragment.testViewModelFactory = PageViewModelFactory(
            openPage = openPage,
            closePage = closePage,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            createPage = createPage,
            documentEventReducer = DocumentExternalEventReducer(),
            archiveDocument = archiveDocument,
            createDocument = createDocument,
            uploadUrl = uploadUrl,
            urlBuilder = urlBuilder,
            renderer = DefaultBlockViewRenderer(
                urlBuilder = urlBuilder,
                counter = Counter.Default(),
                toggleStateHolder = ToggleStateHolder.Default()
            ),
            interactor = Orchestrator(
                createBlock = createBlock,
                splitBlock = splitBlock,
                unlinkBlocks = unlinkBlocks,
                updateCheckbox = updateCheckbox,
                updateTextStyle = updateTextStyle,
                updateText = updateText,
                updateBackgroundColor = updateBackgroundColor,
                undo = undo,
                redo = redo,
                copy = copy,
                paste = paste,
                duplicateBlock = duplicateBlock,
                updateAlignment = updateAlignment,
                downloadFile = downloadFile,
                mergeBlocks = mergeBlocks,
                updateTitle = updateTitle,
                updateTextColor = updateTextColor,
                replaceBlock = replaceBlock,
                setupBookmark = setupBookmark,
                memory = Editor.Memory(
                    selections = SelectionStateHolder.Default()
                ),
                stores = stores,
                proxies = proxies,
                textInteractor = Interactor.TextInteractor(
                    proxies = proxies,
                    stores = stores,
                    matcher = DefaultPatternMatcher()
                )
            )
        )
    }

    @Test()
    fun shouldSetTextForTextBlocks() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val blocks = listOf(
            BLOCK_H1,
            BLOCK_H2,
            BLOCK_H3,
            BLOCK_PARAGRAPH,
            BLOCK_HIGHLIGHT,
            BLOCK_BULLET,
            BLOCK_NUMBERED_1,
            BLOCK_TOGGLE,
            BLOCK_CHECKBOX
        )

        val document = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = blocks.map { it.id }
            )
        ) + blocks

        stubInterceptEvents()
        stubOpenDocument(document)

        launchFragment(args)

        // TESTING

        onView(withId(R.id.recycler)).check(matches(isDisplayed()))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.headerOne))
            .check(matches(withText(BLOCK_H1.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(2, R.id.headerTwo))
            .check(matches(withText(BLOCK_H2.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(3, R.id.headerThree))
            .check(matches(withText(BLOCK_H3.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(4, R.id.textContent))
            .check(matches(withText(BLOCK_PARAGRAPH.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(5, R.id.highlightContent))
            .check(matches(withText(BLOCK_HIGHLIGHT.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(6, R.id.bulletedListContent))
            .check(matches(withText(BLOCK_BULLET.content.asText().text)))

        R.id.recycler.scrollTo<BlockViewHolder.Numbered>(7)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(7, R.id.numberedListContent))
            .check(matches(withText(BLOCK_NUMBERED_1.content.asText().text)))

        onView(withRecyclerView(R.id.recycler).atPositionOnView(7, R.id.number))
            .check(matches(withText("1.")))

        R.id.recycler.scrollTo<BlockViewHolder.Toggle>(8)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(8, R.id.toggleContent))
            .check(matches(withText(BLOCK_TOGGLE.content.asText().text)))

        R.id.recycler.scrollTo<BlockViewHolder.Checkbox>(9)

        onView(withRecyclerView(R.id.recycler).atPositionOnView(9, R.id.checkboxContent))
            .check(matches(withText(BLOCK_CHECKBOX.content.asText().text)))
    }

    @Test
    fun shouldAppendTextToTheEndAfterTyping() {

        // SETUP

        val args = bundleOf(PageFragment.ID_KEY to root)

        val document = listOf(
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

        stubInterceptEvents()
        stubOpenDocument(document)

        updateText.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }

        launchFragment(args)

        // TESTING

        val text = " Add new text at the end"

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent))

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
    fun shouldClearFocusAfterClickedOnHideKeyboard() {

        val args = bundleOf(PageFragment.ID_KEY to root)

        val document = listOf(
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

        stubInterceptEvents()
        stubOpenDocument(document)

        launchFragment(args)

        // TESTING

        val target = onView(withRecyclerView(R.id.recycler).atPositionOnView(1, R.id.textContent))

        // Focusing

        target.perform(click())

        onView(withId(R.id.toolbar)).check(matches((isDisplayed())))
        target.check(matches((hasFocus())))

        // Unfocusing

        onView(allOf(withId(R.id.unfocus), isDisplayed())).perform(click())

        onView(withId(R.id.toolbar)).check(matches(not(isDisplayed())))
        target.check(matches(not(hasFocus())))
    }

    /*

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

    private fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build() } doReturn emptyFlow()
        }
    }

    private fun stubOpenDocument(document: List<Block>) {
        openPage.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = listOf(
                        Event.Command.ShowBlock(
                            context = root,
                            root = root,
                            details = Block.Details(),
                            blocks = document
                        )
                    )
                )
            )
        }
    }

    /**
     * Moves coroutines clock time.
     */
    private fun advance(millis: Long) {
        coroutineTestRule.advanceTime(millis)
    }
}