package com.agileburo.anytype.presentation.page

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.config.MainConfig
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.MockBlockFactory
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.page.PageViewModel.ViewState
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class PageViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Mock
    lateinit var openPage: OpenPage

    @Mock
    lateinit var closePage: ClosePage

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateBlock: UpdateBlock

    @Mock
    lateinit var updateCheckbox: UpdateCheckbox

    @Mock
    lateinit var duplicateBlock: DuplicateBlock

    @Mock
    lateinit var unlinkBlocks: UnlinkBlocks

    @Mock
    lateinit var updateTextStyle: UpdateTextStyle

    @Mock
    lateinit var updateTextColor: UpdateTextColor

    @Mock
    lateinit var updateLinkMark: UpdateLinkMarks

    @Mock
    lateinit var removeLinkMark: RemoveLinkMark

    @Mock
    lateinit var mergeBlocks: MergeBlocks

    @Mock
    lateinit var splitBlock: SplitBlock

    @Mock
    lateinit var createPage: CreatePage

    @Mock
    lateinit var updateBackgroundColor: UpdateBackgroundColor

    private lateinit var vm: PageViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should start observing events when view model is initialized`() {
        stubObserveEvents()
        buildViewModel()
        verify(interceptEvents, times(1)).build()
    }

    @Test
    fun `should start opening page when requested`() {

        val id = MockDataFactory.randomUuid()

        val param = OpenPage.Params(id = id)

        stubObserveEvents()
        buildViewModel()

        vm.open(id)

        verify(openPage, times(1)).invoke(any(), argThat { this.id == param.id }, any())
    }

    @Test
    fun `should dispatch a page to UI when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(child)
            ),
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        openPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        val flow: Flow<List<Event>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)

        openPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        val expected = ViewState.Success(blocks = listOf(page.last().toView()))
        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should close page when the system back button is pressed`() {

        stubObserveEvents()

        buildViewModel()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed()

        verify(closePage, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should emit an approprtiate navigation command when the page is closed`() {

        val response = Either.Right(Unit)

        stubObserveEvents()
        stubClosePage(response)
        buildViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed()

        testObserver
            .assertHasValue()
            .assertValue { value -> value.peekContent() == AppNavigation.Command.Exit }
    }

    @Test
    fun `should not emit any navigation command if there is an error while closing the page`() {

        val error = Exception("Error while closing this page")

        val response = Either.Left(error)

        stubClosePage(response)
        stubObserveEvents()
        buildViewModel()

        val testObserver = vm.navigation.test()

        verifyZeroInteractions(closePage)

        vm.onSystemBackPressed()

        testObserver.assertNoValue()
    }

    @Test
    fun `should update block when its text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        buildViewModel()

        vm.open(pageId)
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateBlock, times(1)).invoke(
            any(),
            argThat { this.contextId == pageId && this.blockId == blockId && this.text == text },
            any()
        )
    }

    @Test
    fun `should debonce values when dispatching text changes`() {

        val blockId = MockDataFactory.randomUuid()
        val pageId = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        stubObserveEvents()
        buildViewModel()

        vm.open(pageId)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())
        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        vm.onTextChanged(id = blockId, text = text, marks = emptyList())

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateBlock, times(2)).invoke(
            any(),
            argThat { this.contextId == pageId && this.blockId == blockId && this.text == text },
            any()
        )
    }

    @Test
    fun `should add a new block to the already existing one when this view model receives an appropriate command`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(child)
            ),
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        val added = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        stubObserveEvents(
            flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateStructure(
                            context = root,
                            id = root,
                            children = listOf(child, added.id)
                        )
                    )
                )
                emit(
                    listOf(
                        Event.Command.AddBlock(
                            blocks = listOf(added),
                            context = root
                        )
                    )
                )
            }
        )

        stubOpenPage()
        buildViewModel()
        vm.open(root)

        coroutineTestRule.advanceTime(200)

        val expected = ViewState.Success(listOf(page.last().toView(), added.toView(focused = true)))

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start creating a new block if user clicked create-text-block-button`() {
        simulateNormalPageOpeningFlow()
        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)
        verify(createBlock, times(1)).invoke(any(), any(), any())
    }

    @Test
    fun `should update block text without dispatching it to UI when we receive an appropriate event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Page(
                    style = Block.Content.Page.Style.SET
                ),
                children = listOf(child)
            ),
            Block(
                id = child,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                children = emptyList()
            )
        )

        val text = MockDataFactory.randomString()

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    listOf(
                        Event.Command.ShowBlock(
                            rootId = root,
                            blocks = page,
                            context = root
                        )
                    )
                )
                delay(100)
                emit(
                    listOf(
                        Event.Command.UpdateBlockText(
                            text = text,
                            id = child,
                            context = root
                        )
                    )
                )
            }
        }

        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val beforeUpdate = ViewState.Success(
            listOf(page.last().toView())
        )

        vm.state.test().assertValue(beforeUpdate)

        coroutineTestRule.advanceTime(200)

        val afterUpdate = beforeUpdate.copy()

        vm.state.test().assertValue(afterUpdate)
    }

    @Test
    fun `should emit loading state when starting opening a page`() {

        val root = MockDataFactory.randomUuid()

        stubOpenPage()
        stubObserveEvents()
        buildViewModel()

        val testObserver = vm.state.test()

        testObserver.assertNoValue()

        vm.open(root)

        testObserver.assertValue(ViewState.Loading)
    }

    @Test
    fun `should apply two different markup actions`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
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

        stubOpenPage()

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = Markup.Type.BOLD

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onMarkupActionClicked(firstTimeMarkup)

        val firstTimeExpected = ViewState.Success(
            listOf(
                paragraph
                    .copy(
                        content = (paragraph.content as Block.Content.Text).copy(
                            marks = listOf(
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.BOLD,
                                    param = null,
                                    range = firstTimeRange
                                )
                            )
                        )
                    )
                    .toView(focused = true)
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        val secondTimeRange = 0..5
        val secondTimeMarkup = Markup.Type.ITALIC

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onMarkupActionClicked(secondTimeMarkup)

        val secondTimeExpected = ViewState.Success(
            listOf(
                paragraph
                    .copy(
                        content = (paragraph.content as Block.Content.Text).copy(
                            marks = listOf(
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.BOLD,
                                    param = null,
                                    range = firstTimeRange
                                ),
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.ITALIC,
                                    param = null,
                                    range = secondTimeRange
                                )
                            )
                        )
                    )
                    .toView(focused = true)
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should apply two markup actions of the same markup type`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
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

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val firstTimeRange = 0..3
        val firstTimeMarkup = Markup.Type.BOLD

        vm.onBlockFocusChanged(
            hasFocus = true,
            id = paragraph.id
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = firstTimeRange
        )

        vm.onMarkupActionClicked(firstTimeMarkup)

        val firstTimeExpected = ViewState.Success(
            listOf(
                paragraph
                    .copy(
                        content = (paragraph.content as Block.Content.Text).copy(
                            marks = listOf(
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.BOLD,
                                    param = null,
                                    range = firstTimeRange
                                )
                            )
                        )
                    )
                    .toView(focused = true)
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(firstTimeExpected)
        }

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 3..3
        )

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = 0..0
        )

        val secondTimeRange = 0..5
        val secondTimeMarkup = Markup.Type.BOLD

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = secondTimeRange
        )

        vm.onMarkupActionClicked(secondTimeMarkup)

        val secondTimeExpected = ViewState.Success(
            listOf(
                paragraph
                    .copy(
                        content = (paragraph.content as Block.Content.Text).copy(
                            marks = listOf(
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.BOLD,
                                    param = null,
                                    range = secondTimeRange
                                )
                            )
                        )
                    )
                    .toView(focused = true)
            )
        )

        vm.state.test().apply {
            assertHasValue()
            assertValue(secondTimeExpected)
        }
    }

    @Test
    fun `should dispatch texts changes and markup even if only markup is changed`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
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

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val range = 0..3
        val markup = Markup.Type.BOLD

        vm.onSelectionChanged(
            id = paragraph.id,
            selection = range
        )

        vm.onMarkupActionClicked(markup)

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                param = null,
                range = range
            )
        )

        verify(updateBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                UpdateBlock.Params(
                    blockId = paragraph.id,
                    marks = marks,
                    contextId = page.id,
                    text = paragraph.content.asText().text
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `test changes from UI do not trigger re-rendering`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
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

        stubObserveEvents(events)
        stubOpenPage()
        buildViewModel()

        val testObserver = vm.state.test()

        vm.open(root)

        testObserver.assertValue(ViewState.Loading)

        coroutineTestRule.advanceTime(100)

        val state = ViewState.Success(
            listOf(paragraph.toView())
        )

        testObserver
            .assertValue(state)
            .assertHistorySize(2)

        val userInput = MockDataFactory.randomString()

        val range = 0..3

        val marks = listOf(
            Block.Content.Text.Mark(
                type = Block.Content.Text.Mark.Type.BOLD,
                param = null,
                range = range
            )
        )

        vm.onTextChanged(
            id = paragraph.id,
            marks = marks,
            text = userInput
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        testObserver
            .assertValue(state)
            .assertHistorySize(2)
    }

    @Test
    fun `should update text inside view state when user changed text`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        val events = flow {
            delay(100)
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

        stubObserveEvents(events)
        stubOpenPage()

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = emptyList())

        val contentAfterChange = Block.Content.Text(
            text = userInput,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraphAfterChange = paragraph.copy(
            content = contentAfterChange
        )

        val expected = listOf(
            page,
            paragraphAfterChange
        )

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        assertEquals(
            expected = expected,
            actual = vm.blocks
        )
    }

    @Test
    fun `should dispatch text changes including markup to the middleware`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val initialText = ""

        val initialContent = Block.Content.Text(
            text = initialText,
            marks = emptyList(),
            style = Block.Content.Text.Style.P
        )

        val paragraph = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = initialContent,
            children = emptyList()
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(child)
        )

        val blocks = listOf(
            page,
            paragraph
        )

        interceptEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
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

        stubOpenPage()

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val userInput = MockDataFactory.randomString()
        val marks = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        vm.onTextChanged(id = paragraph.id, text = userInput, marks = marks)

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verify(updateBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                UpdateBlock.Params(
                    blockId = paragraph.id,
                    text = userInput,
                    marks = marks,
                    contextId = page.id
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should receive initial control panel state when view model is initialized`() {
        simulateNormalPageOpeningFlow()
        val expected = ControlPanelState.init()
        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `add-block-or-turn-into panel should be opened on add-block-toolbar-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )
        vm.onAddBlockToolbarClicked()

        val expected = ControlPanelState(
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = true
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            focus = ControlPanelState.Focus(
                id = child,
                type = ControlPanelState.Focus.Type.P
            )
        )

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `add-block-or-turn-into panel should be closed on add-text-block-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )
        vm.onAddBlockToolbarClicked()
        vm.onAddTextBlockClicked(style = Block.Content.Text.Style.P)

        val expected = ControlPanelState(
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            focus = ControlPanelState.Focus(
                id = child,
                type = ControlPanelState.Focus.Type.P
            )
        )

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should open markup panel when text is selected`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )
        vm.onSelectionChanged(
            id = MockDataFactory.randomUuid(),
            selection = 0..4
        )

        val expectedVisibility = true

        val expected = ControlPanelState(
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = false,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = expectedVisibility
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            focus = ControlPanelState.Focus(
                id = child,
                type = ControlPanelState.Focus.Type.P
            )
        )

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should not open markup panel when text selection is empty`() {

        val expectedVisibility = false

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onSelectionChanged(
            id = MockDataFactory.randomUuid(),
            selection = 0..0
        )

        val expected = ControlPanelState(
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = expectedVisibility
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            focus = ControlPanelState.Focus(
                id = child,
                type = ControlPanelState.Focus.Type.P
            )
        )

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should open color toolbar when this option is selected on markup toolbar`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onSelectionChanged(
            id = MockDataFactory.randomUuid(),
            selection = 0..4
        )

        vm.onMarkupToolbarColorClicked()

        val expectedVisibility = true

        val expected = ControlPanelState(
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = expectedVisibility
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = false,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Markup.Action.COLOR
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            focus = ControlPanelState.Focus(
                id = child,
                type = ControlPanelState.Focus.Type.P
            )
        )

        vm.controlPanelViewState.test().assertValue(expected)
    }

    @Test
    fun `should add a header-one block on add-header-one event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val style = Block.Content.Text.Style.H1

        val new = Block(
            id = MockDataFactory.randomString(),
            content = Block.Content.Text(
                text = "",
                marks = emptyList(),
                style = style
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val flow: Flow<List<Event>> = flow {
            delay(500)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        rootId = root,
                        blocks = page,
                        context = root
                    )
                )
            )
            delay(500)
            emit(
                listOf(
                    Event.Command.UpdateStructure(
                        context = root,
                        id = root,
                        children = listOf(child, new.id)
                    )
                )
            )
            emit(
                listOf(
                    Event.Command.AddBlock(
                        blocks = listOf(new),
                        context = root
                    )
                )
            )
        }

        stubObserveEvents(flow)
        stubOpenPage()

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(500)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(page.last().toView(false))
            )
        )

        coroutineTestRule.advanceTime(500)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    page.last().toView(focused = false),
                    new.toView(focused = true)
                )
            )
        )
    }

    @Test
    fun `should start updating checkbox when it is clicked`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.CHECKBOX
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1000)

        vm.onCheckboxClicked(page.last().id)

        verify(updateCheckbox, times(1)).invoke(
            any(),
            eq(
                UpdateCheckbox.Params(
                    context = root,
                    target = child,
                    isChecked = true
                )
            ),
            any()
        )
    }

    @Test
    fun `should start duplicating focused block when requested`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child, hasFocus = true)
        vm.onActionDuplicateClicked()

        verify(duplicateBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                DuplicateBlock.Params(
                    original = child,
                    context = root
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should start deleting focused block when requested`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)

        vm.onBlockFocusChanged(id = child, hasFocus = true)
        vm.onActionDeleteClicked()

        verify(unlinkBlocks, times(1)).invoke(
            scope = any(),
            params = eq(
                UnlinkBlocks.Params(
                    context = root,
                    targets = listOf(child)
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should delete the first block when the delete-block event received for the first block, then rerender the page`() {

        val pageOpenedDelay = 100L
        val blockDeletedEventDelay = 100L

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(pageOpenedDelay)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        rootId = root,
                        blocks = page,
                        context = root
                    )
                )
            )
            delay(blockDeletedEventDelay)
            emit(
                listOf(
                    Event.Command.DeleteBlock(
                        targets = listOf(firstChild),
                        context = root
                    )
                )
            )
        }

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(pageOpenedDelay)

        val testObserver = vm.state.test()

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    page[1].toView(focused = false),
                    page.last().toView(focused = false)
                )
            )
        )

        vm.onBlockFocusChanged(id = firstChild, hasFocus = true)
        vm.onActionDeleteClicked()

        assertEquals(expected = 3, actual = vm.blocks.size)

        coroutineTestRule.advanceTime(blockDeletedEventDelay)

        assertEquals(expected = 2, actual = vm.blocks.size)

        testObserver.assertValue(
            ViewState.Success(
                blocks = listOf(
                    page.last().toView(focused = false)
                )
            )
        )
    }

    @Test
    fun `should start deleting the target block on empty-block-backspace-click event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, times(1)).invoke(
            scope = any(),
            params = eq(
                UnlinkBlocks.Params(
                    context = root,
                    targets = listOf(child)
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should not proceed with deleting the title block on empty-block-backspace-click event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onEmptyBlockBackspaceClicked(child)

        verify(unlinkBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new block on end-line-enter-press event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val events: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubOpenPage()
        stubObserveEvents(events)
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    position = Position.BOTTOM,
                    prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if page contains only title and icon`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onOutsideClicked()

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = "",
                    position = Position.INNER,
                    prototype = Block.Prototype.Text(
                        style = Block.Content.Text.Style.P
                    )
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should not create a new paragraph on outside-clicked event if page already contains one paragraph`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChild = secondChild,
            secondChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onOutsideClicked()

        verify(createBlock, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should start updating text style of the focused block on turn-into-option-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChild = secondChild,
            secondChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        val newStyle = Block.Content.Text.Style.H1

        vm.onTurnIntoStyleClicked(style = newStyle)

        verify(updateTextStyle, times(1)).invoke(
            scope = any(),
            params = eq(
                UpdateTextStyle.Params(
                    context = root,
                    target = secondChild,
                    style = newStyle
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should clear focus internally and re-render on hide-keyboard event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val testObserver = vm.focus.test()

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        testObserver.assertValue(child)

        vm.onHideKeyboardClicked()

        testObserver.assertValue(PageViewModel.EMPTY_FOCUS_ID)
    }

    @Test
    fun `should start updating the target block's color on color-toolbar-option-selected event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = Block.Content.Text.Style.TITLE
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val color = MockDataFactory.randomString()

        vm.onToolbarTextColorAction(color = color)

        verify(updateTextColor, times(1)).invoke(
            scope = any(),
            params = eq(
                UpdateTextColor.Params(
                    context = root,
                    target = child,
                    color = color
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should start creating a new bulleted-list item on endline-enter-pressed event inside a bullet block`() {

        val style = Block.Content.Text.Style.BULLET
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = style
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    prototype = Block.Prototype.Text(
                        style = style
                    ),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should start creating a new checkbox item on endline-enter-pressed event inside a bullet block`() {

        val style = Block.Content.Text.Style.CHECKBOX
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = style
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            marks = emptyList(),
            text = page.last().content<Block.Content.Text>().text
        )

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    prototype = Block.Prototype.Text(
                        style = style
                    ),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should start creating a new paragraph on endline-enter-pressed event inside a quote block`() {

        val style = Block.Content.Text.Style.QUOTE
        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child,
            style = style
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = child,
            text = page.last().content<Block.Content.Text>().text,
            marks = emptyList()
        )

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    context = root,
                    target = child,
                    prototype = Block.Prototype.Text(
                        style = Block.Content.Text.Style.P
                    ),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should not proceed with merging page title with the first paragraph on non-empty-block-backspace-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        vm.onNonEmptyBlockBackspaceClicked(
            id = secondChild
        )

        verify(mergeBlocks, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with merging the first paragraph with the second on non-empty-block-backspace-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()
        val thirdChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithThreeTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            thirdChild = thirdChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.P,
            thirdChildStyle = Block.Content.Text.Style.P
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = thirdChild,
            hasFocus = true
        )

        vm.onNonEmptyBlockBackspaceClicked(
            id = thirdChild
        )

        verify(mergeBlocks, times(1)).invoke(
            scope = any(),
            params = eq(
                MergeBlocks.Params(
                    context = root,
                    pair = Pair(secondChild, thirdChild)
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should turn a list item with empty text into a paragraph on endline-enter-pressed event`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTwoTextBlocks(
            root = root,
            firstChild = firstChild,
            secondChild = secondChild,
            firstChildStyle = Block.Content.Text.Style.TITLE,
            secondChildStyle = Block.Content.Text.Style.BULLET,
            secondChildText = ""
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        vm.onBlockFocusChanged(
            id = secondChild,
            hasFocus = true
        )

        vm.onEndLineEnterClicked(
            id = secondChild,
            text = "",
            marks = emptyList()
        )

        verify(createBlock, never()).invoke(
            scope = any(),
            params = any(),
            onResult = any()
        )
        verify(updateTextStyle, times(1)).invoke(
            scope = any(),
            params = eq(
                UpdateTextStyle.Params(
                    target = secondChild,
                    style = Block.Content.Text.Style.P,
                    context = root
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should create a new paragraph on outside-clicked event if the last block is a link block`() {

        val root = MockDataFactory.randomUuid()
        val firstChild = MockDataFactory.randomUuid()
        val secondChild = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithTitleAndOnePageLinkBlock(
            rootId = root,
            titleBlockId = firstChild,
            pageBlockId = secondChild
        )

        val startDelay = 100L

        val flow: Flow<List<Event.Command>> = flow {
            delay(startDelay)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(startDelay)

        vm.onOutsideClicked()

        verify(createBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                CreateBlock.Params(
                    target = "",
                    context = root,
                    position = Position.INNER,
                    prototype = Block.Prototype.Text(
                        style = Block.Content.Text.Style.P
                    )
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with splittig block on split-enter-key event`() {

        // SETUP

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onBlockFocusChanged(
            id = child,
            hasFocus = true
        )

        val index = MockDataFactory.randomInt()

        vm.onSplitLineEnterClicked(
            id = child,
            index = index
        )

        verify(splitBlock, times(1)).invoke(
            scope = any(),
            params = eq(
                SplitBlock.Params(
                    context = root,
                    target = child,
                    index = index
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should proceed with creating a new page on on-plus-button-clicked event`() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()

        val page = MockBlockFactory.makeOnePageWithOneTextBlock(
            root = root,
            child = child
        )

        val flow: Flow<List<Event.Command>> = flow {
            delay(100)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        // TESTING

        vm.onPlusButtonPressed()

        verify(createPage, times(1)).invoke(
            scope = any(),
            params = eq(
                CreatePage.Params(
                    id = MainConfig.HOME_DASHBOARD_ID
                )
            ),
            onResult = any()
        )
    }

    private fun simulateNormalPageOpeningFlow() {

        val root = MockDataFactory.randomUuid()
        val child = MockDataFactory.randomUuid()
        val page = MockBlockFactory.makeOnePageWithOneTextBlock(root = root, child = child)

        val flow: Flow<List<Event.Command>> = flow {
            delay(1000)
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

        stubObserveEvents(flow)
        stubOpenPage()
        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(1001)
    }

    private fun stubClosePage(response: Either<Throwable, Unit>) {
        closePage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(response)
            }
        }
    }

    private fun stubOpenPage() {
        openPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }
    }

    private fun stubObserveEvents(flow: Flow<List<Event>> = flowOf()) {
        interceptEvents.stub {
            onBlocking { build() } doReturn flow
        }
    }

    private fun buildViewModel() {
        vm = PageViewModel(
            openPage = openPage,
            closePage = closePage,
            createPage = createPage,
            updateBlock = updateBlock,
            interceptEvents = interceptEvents,
            createBlock = createBlock,
            updateCheckbox = updateCheckbox,
            unlinkBlocks = unlinkBlocks,
            duplicateBlock = duplicateBlock,
            updateTextStyle = updateTextStyle,
            updateTextColor = updateTextColor,
            updateBackgroundColor = updateBackgroundColor,
            updateLinkMarks = updateLinkMark,
            removeLinkMark = removeLinkMark,
            mergeBlocks = mergeBlocks,
            splitBlock = splitBlock
        )
    }
}