package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetAddOrUpdateViewerTest : ObjectSetViewModelTestSetup() {

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should add new viewer on appropriate event`() {

        // SETUP

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            )
        )

        val vm = buildViewModel()

        // TESTING

        val new = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = emptyList()
        )

        val event = Event.Command.DataView.SetView(
            context = root,
            viewerId = MockDataFactory.randomUuid(),
            viewer = new,
            limit = MockDataFactory.randomInt(),
            offset = MockDataFactory.randomInt(),
            target = dv.id
        )

        vm.onStart(root)

        val expectedBefore = listOf(
            ViewerTabView(
                id = viewer.id,
                name = viewer.name,
                isActive = true
            )
        )

        assertEquals(
            expected = expectedBefore,
            actual = vm.viewerTabs.value
        )

        // Simulating external event sent from middleware (which results in adding a new view to DV)

        runBlocking {
            dispatcher.send(
                Payload(
                    context = root,
                    events = listOf(event)
                )
            )
        }

        val expectedAfter = listOf(
            ViewerTabView(
                id = viewer.id,
                name = viewer.name,
                isActive = true
            ),
            ViewerTabView(
                id = new.id,
                name = new.name,
                isActive = false
            )
        )

        assertEquals(
            expected = expectedAfter,
            actual = vm.viewerTabs.value
        )
    }

    @Test
    fun `should update current viewer on appropriate event`() {

        // SETUP

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            )
        )

        val vm = buildViewModel()

        // TESTING

        val newName = MockDataFactory.randomString()

        val event = Event.Command.DataView.SetView(
            context = root,
            viewerId = viewer.id,
            viewer = viewer.copy(name = newName),
            limit = MockDataFactory.randomInt(),
            offset = MockDataFactory.randomInt(),
            target = dv.id
        )

        vm.onStart(root)

        val expectedBefore = listOf(
            ViewerTabView(
                id = viewer.id,
                name = viewer.name,
                isActive = true
            )
        )

        assertEquals(
            expected = expectedBefore,
            actual = vm.viewerTabs.value
        )

        // Simulating external event sent from middleware (which results in adding a new view to DV)

        runBlocking {
            dispatcher.send(
                Payload(
                    context = root,
                    events = listOf(event)
                )
            )
        }

        val expectedAfter = listOf(
            ViewerTabView(
                id = viewer.id,
                name = newName,
                isActive = true
            )
        )

        assertEquals(
            expected = expectedAfter,
            actual = vm.viewerTabs.value
        )
    }
}