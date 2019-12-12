package com.agileburo.anytype.presentation.page

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.CreateBlock
import com.agileburo.anytype.domain.block.interactor.UpdateBlock
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.interactor.ObserveEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.page.PageViewModel.ViewState
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

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
    lateinit var observeEvents: ObserveEvents

    @Mock
    lateinit var createBlock: CreateBlock

    @Mock
    lateinit var updateBlock: UpdateBlock

    private lateinit var vm: PageViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should start observing events when view model is initialized`() = runBlockingTest {
        stubObserveEvents()
        buildViewModel()
        verify(observeEvents, times(1)).build()
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

        val flow: Flow<Event.Command> = flow {
            delay(1000)
            emit(
                Event.Command.ShowBlock(
                    rootId = root,
                    blocks = page
                )
            )
        }

        observeEvents.stub {
            onBlocking { build() } doReturn flow
        }

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
        vm.onTextChanged(id = blockId, text = text)

        coroutineTestRule.advanceTime(500L)

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

        vm.onTextChanged(id = blockId, text = text)
        vm.onTextChanged(id = blockId, text = text)
        vm.onTextChanged(id = blockId, text = text)

        coroutineTestRule.advanceTime(500L)

        vm.onTextChanged(id = blockId, text = text)

        coroutineTestRule.advanceTime(500L)

        verify(updateBlock, times(2)).invoke(
            any(),
            argThat { this.contextId == pageId && this.blockId == blockId && this.text == text },
            any()
        )
    }

    @Test
    fun `should add a new block when this view model receives a command to do that`() {

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

        observeEvents.stub {
            onBlocking { build() } doReturn flowOf(
                Event.Command.AddBlock(
                    blocks = listOf(added)
                )
            )
        }

        buildViewModel()

        val expected = ViewState.Success(listOf(added.toView()))

        vm.state.test().assertValue(expected)
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

        observeEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    Event.Command.ShowBlock(
                        rootId = root,
                        blocks = page
                    )
                )
                emit(
                    Event.Command.AddBlock(
                        blocks = listOf(added)
                    )
                )
            }
        }

        openPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(200)

        val expected = ViewState.Success(listOf(page.last().toView(), added.toView()))

        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start creating a new block if user clicked create-text-block-button`() {

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

        observeEvents.stub {
            onBlocking { build() } doReturn flowOf(
                Event.Command.ShowBlock(
                    rootId = root,
                    blocks = page
                )
            )
        }

        buildViewModel()

        vm.onAddTextBlockClicked()

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

        observeEvents.stub {
            onBlocking { build() } doReturn flow {
                delay(100)
                emit(
                    Event.Command.ShowBlock(
                        rootId = root,
                        blocks = page
                    )
                )
                delay(100)
                emit(
                    Event.Command.UpdateBlockText(
                        text = text,
                        id = child
                    )
                )
            }
        }

        openPage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(Either.Right(Unit))
            }
        }

        buildViewModel()

        vm.open(root)

        coroutineTestRule.advanceTime(100)

        val beforeUpdate = ViewState.Success(
            listOf(page.last().toView())
        )

        vm.state.test().assertValue(beforeUpdate)

        coroutineTestRule.advanceTime(200)

        val expected = page.last().copy(
            content = (page.last().content as Block.Content.Text).copy(
                text = text
            )
        )

        val afterUpdate = beforeUpdate.copy()

        vm.state.test().assertValue(afterUpdate)
    }

    private fun stubClosePage(response: Either<Throwable, Unit>) {
        closePage.stub {
            onBlocking { invoke(any(), any(), any()) } doAnswer { answer ->
                answer.getArgument<(Either<Throwable, Unit>) -> Unit>(2)(response)
            }
        }
    }

    private fun stubObserveEvents() {
        observeEvents.stub {
            onBlocking { build() } doReturn flowOf()
        }
    }

    private fun buildViewModel() {
        vm = PageViewModel(
            openPage = openPage,
            closePage = closePage,
            updateBlock = updateBlock,
            observeEvents = observeEvents,
            createBlock = createBlock
        )
    }
}