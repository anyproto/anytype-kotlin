package com.agileburo.anytype.domain.dashboard

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.CoroutineTestRule
import com.agileburo.anytype.domain.common.MockDataFactory
import com.agileburo.anytype.domain.dashboard.interactor.ObserveHomeDashboard
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.domain.event.model.Event
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class ObserveHomeDashboardTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    lateinit var useCase: ObserveHomeDashboard

    @Mock
    lateinit var repo: BlockRepository

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        useCase = ObserveHomeDashboard(
            context = TestCoroutineDispatcher(),
            repo = repo
        )
    }

    @Test
    fun `should ignore other events`() = runBlockingTest {

        val id = MockDataFactory.randomUuid()

        val param = ObserveHomeDashboard.Param(id = id)

        val events = flowOf(
            Event.Command.UpdateBlockText(
                id = MockDataFactory.randomUuid(),
                text = MockDataFactory.randomString()
            )
        )

        stubObserveEvents(events)

        val result = mutableListOf<HomeDashboard>()

        useCase.build(param).toList(result)

        assertTrue { result.isEmpty() }
    }

    @Test
    fun `should process event and map it to home dashboard`() = runBlockingTest {

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.EMPTY
            ),
            fields = Block.Fields(
                map = mapOf("name" to MockDataFactory.randomString())
            )
        )

        val dashboard = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(page.id),
            content = Block.Content.Dashboard(
                type = Block.Content.Dashboard.Type.MAIN_SCREEN
            ),
            fields = Block.Fields(
                map = mapOf("name" to MockDataFactory.randomString())
            )
        )

        val event = Event.Command.ShowBlock(
            rootId = dashboard.id,
            blocks = listOf(dashboard, page)
        )

        val param = ObserveHomeDashboard.Param(
            id = dashboard.id
        )

        val flow = flowOf(event)

        stubObserveEvents(flow)

        val result = mutableListOf<HomeDashboard>()
        val expected = HomeDashboard(
            id = dashboard.id,
            fields = dashboard.fields,
            children = dashboard.children,
            blocks = listOf(page),
            type = dashboard.content.asDashboard().type
        )

        useCase.build(param).toList(result)

        assertTrue {
            result.first() == expected
        }
    }

    @Test
    fun `should ignore events not related to dashboard`() = runBlockingTest {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(title.id),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            fields = Block.Fields(
                map = mapOf("name" to MockDataFactory.randomString())
            )
        )

        val event = Event.Command.ShowBlock(
            rootId = page.id,
            blocks = listOf(page, title)
        )

        val param = ObserveHomeDashboard.Param(
            id = page.id
        )

        val flow = flowOf(event)

        stubObserveEvents(flow)

        val result = mutableListOf<HomeDashboard>()

        useCase.build(param).toList(result)

        assertTrue { result.isEmpty() }
    }

    private fun stubObserveEvents(events: Flow<Event>) {
        repo.stub {
            onBlocking { observeEvents() } doReturn events
        }
    }
}