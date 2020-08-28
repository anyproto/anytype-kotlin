package com.agileburo.anytype.presentation.home

import MockDataFactory
import com.agileburo.anytype.core_utils.ext.shift
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.dashboard.interactor.toHomeDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine
import com.agileburo.anytype.presentation.mapper.toView
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class DashboardDragAndDropTest : DashboardTestSetup() {

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `block dragging events do not alter overall state`() {

        // SETUP

        val pages = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    target = MockDataFactory.randomUuid(),
                    type = Block.Content.Link.Type.PAGE,
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                            "icon" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    target = MockDataFactory.randomUuid(),
                    type = Block.Content.Link.Type.PAGE,
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString()
                        )
                    )
                )
            )
        )

        val dashboard = Block(
            id = config.home,
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.HOME
            ),
            children = pages.map { page -> page.id },
            fields = Block.Fields.empty()
        )

        val delayInMillis = 100L

        val events = flow {
            delay(delayInMillis)
            emit(
                listOf(
                    Event.Command.ShowBlock(
                        root = config.home,
                        context = config.home,
                        blocks = listOf(dashboard) + pages
                    )
                )
            )
        }

        stubGetConfig(
            Either.Right(config)
        )

        stubObserveEvents(
            params = InterceptEvents.Params(context = config.home),
            flow = events
        )

        stubOpenDashboard(
            payload = Payload(
                context = config.home,
                events = emptyList()
            )
        )

        // TESTING

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val expected = HomeDashboardStateMachine.State(
            isLoading = false,
            isInitialzed = true,
            dashboard = listOf(
                dashboard,
                pages.first(),
                pages.last()
            ).toHomeDashboard(dashboard.id),
            error = null
        )

        val views = runBlocking {
            listOf(
                dashboard,
                pages.first(),
                pages.last()
            ).toHomeDashboard(dashboard.id).toView(builder)
        }

        val from = 0
        val to = 1

        vm.state.test().assertValue(expected)

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        verifyZeroInteractions(move)
        vm.state.test().assertValue(expected)
    }

    @Test
    fun `should start dispatching drag-and-drop actions when the dragged item is dropped`() {

        val pages = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            )
        )

        val dashboard = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Smart.Type.HOME,
            blocks = pages,
            children = pages.map { it.id }
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val views = runBlocking {
            dashboard.toView(builder)
        }

        val from = 0
        val to = 1

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = pages.last().id,
                    blockIds = listOf(pages.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the last block before the first block`() {

        val links = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            )
        )

        val dashboard = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Smart.Type.HOME,
            blocks = links,
            children = links.map { it.id }
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val views = runBlocking { dashboard.toView(builder) }

        val from = 2
        val to = 0

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links.first().id,
                    blockIds = listOf(links.last().id),
                    position = Position.TOP
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the first block after the second block`() {

        val links = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            )
        )

        val dashboard = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Smart.Type.HOME,
            blocks = links,
            children = links.map { it.id }
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val views = runBlocking { dashboard.toView(builder) }

        val from = 0
        val to = 1

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links[1].id,
                    blockIds = listOf(links.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }

    @Test
    fun `should call move use-case for dropping the first block after the third block`() {

        val links = listOf(
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString()
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            ),
            Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
                content = Block.Content.Link(
                    type = Block.Content.Link.Type.PAGE,
                    target = MockDataFactory.randomUuid(),
                    fields = Block.Fields(
                        map = mapOf(
                            "name" to MockDataFactory.randomString(),
                        )
                    )
                )
            )
        )

        val dashboard = HomeDashboard(
            id = config.home,
            fields = Block.Fields(map = mapOf("name" to MockDataFactory.randomString())),
            type = Block.Content.Smart.Type.HOME,
            blocks = links,
            children = links.map { it.id }
        )

        val delayInMillis = 100L

        stubGetConfig(Either.Right(config))
        stubObserveEvents(params = InterceptEvents.Params(context = config.home))
        stubOpenDashboard()

        vm = buildViewModel()

        vm.onViewCreated()

        coroutineTestRule.advanceTime(delayInMillis)

        val views = runBlocking { dashboard.toView(builder) }

        val from = 0
        val to = 2

        vm.onItemMoved(
            from = from,
            to = to,
            views = views.toMutableList().shift(from, to)
        )

        vm.onItemDropped(views[from])

        verify(move, times(1)).invoke(
            scope = any(),
            params = eq(
                Move.Params(
                    context = config.home,
                    targetContext = config.home,
                    targetId = links.last().id,
                    blockIds = listOf(links.first().id),
                    position = Position.BOTTOM
                )
            ),
            onResult = any()
        )
    }
}