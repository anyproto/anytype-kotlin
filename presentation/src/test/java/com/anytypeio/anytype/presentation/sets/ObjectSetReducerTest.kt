package com.anytypeio.anytype.presentation.sets

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubFilter
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubSort
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetReducerTest {

    val context = MockDataFactory.randomUuid()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var reducer: ObjectStateReducer

    @Before
    fun reduce() {
        reducer = DefaultObjectStateReducer()
    }

    @After
    fun after() {
        reducer.clear()
        coroutineTestRule.advanceTime(200)
    }

    @Test
    fun `should update sorts in viewer`() = runTest {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val viewerRelations = listOf(
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            ),
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            ),
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            ),
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            ),
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            )
        )

        val viewerGrid = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = viewerRelations,
            sorts = listOf(
                Block.Content.DataView.Sort(
                    relationKey = viewerRelations[1].key,
                    type = Block.Content.DataView.Sort.Type.DESC
                )
            ),
            filters = listOf()
        )

        val dataView = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.DataView(

                viewers = listOf(viewerGrid)
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val eventOpen = Event.Command.ShowObject(
            context = context,
            root = context,
            blocks = blocks,
            details = details
        )

        val event = Event.Command.DataView.SetView(
            context = context,
            target = dataView.id,
            viewerId = viewerGrid.id,
            viewer = Block.Content.DataView.Viewer(
                id = viewerGrid.id,
                name = "New Title",
                type = viewerGrid.type,
                sorts = listOf(
                    Block.Content.DataView.Sort(
                        relationKey = viewerRelations[1].key,
                        type = Block.Content.DataView.Sort.Type.ASC
                    ),
                    Block.Content.DataView.Sort(
                        relationKey = viewerRelations[2].key,
                        type = Block.Content.DataView.Sort.Type.DESC
                    )
                ),
                filters = listOf(),
                viewerRelations = viewerGrid.viewerRelations
            )
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateSetView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewerGrid.id,
                        name = "New Title",
                        type = viewerGrid.type,
                        viewerRelations = viewerRelations,
                        sorts = listOf(
                            Block.Content.DataView.Sort(
                                relationKey = viewerRelations[1].key,
                                type = Block.Content.DataView.Sort.Type.ASC
                            ),
                            Block.Content.DataView.Sort(
                                relationKey = viewerRelations[2].key,
                                type = Block.Content.DataView.Sort.Type.DESC
                            )
                        ),
                        filters = listOf()
                    )
                )
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView), details = details.details
        )

        assertEquals(expected, stateSetView)
    }

    @Test
    fun `should update list viewer`() = runTest {


        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val viewerRelations = listOf(
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            ),
            Block.Content.DataView.Viewer.ViewerRelation(
                key = MockDataFactory.randomUuid(),
                isVisible = true
            )
        )

        val viewerGrid = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = viewerRelations,
            sorts = listOf(
                Block.Content.DataView.Sort(
                    relationKey = viewerRelations[0].key,
                    type = Block.Content.DataView.Sort.Type.ASC
                )
            ),
            filters = listOf()
        )

        val viewerList = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.LIST,
            viewerRelations = viewerRelations,
            sorts = listOf(),
            filters = listOf()
        )

        val dataView = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.DataView(
                viewers = listOf(viewerGrid, viewerList)
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val eventOpen = Event.Command.ShowObject(
            context = context,
            root = context,
            blocks = blocks,
            details = details
        )

        val event = Event.Command.DataView.SetView(
            context = context,
            target = dataView.id,
            viewerId = viewerList.id,
            viewer = Block.Content.DataView.Viewer(
                id = viewerList.id,
                name = "List Title",
                type = viewerList.type,
                sorts = listOf(
                    Block.Content.DataView.Sort(
                        relationKey = viewerRelations[1].key,
                        type = Block.Content.DataView.Sort.Type.DESC
                    )
                ),
                filters = listOf(),
                viewerRelations = viewerGrid.viewerRelations
            )
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateSetView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewerGrid.id,
                        name = viewerGrid.name,
                        type = viewerGrid.type,
                        viewerRelations = viewerGrid.viewerRelations,
                        sorts = viewerGrid.sorts,
                        filters = viewerGrid.filters
                    ),
                    Block.Content.DataView.Viewer(
                        id = viewerList.id,
                        name = "List Title",
                        type = Block.Content.DataView.Viewer.Type.LIST,
                        viewerRelations = viewerList.viewerRelations,
                        sorts = listOf(
                            Block.Content.DataView.Sort(
                                relationKey = viewerRelations[1].key,
                                type = Block.Content.DataView.Sort.Type.DESC
                            )
                        ),
                        filters = listOf()
                    )
                )
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView),
            details = details.details
        )

        assertEquals(expected, stateSetView)
    }

    @Test
    fun `when getting add, move, update, remove sorts events, should proper update viewer`() = runTest {

        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relationKey4 = MockDataFactory.randomUuid()
        val relationKey5 = MockDataFactory.randomUuid()
        val sortId1 = MockDataFactory.randomUuid()
        val sortId2 = MockDataFactory.randomUuid()
        val sortId3 = MockDataFactory.randomUuid()
        val sortId4 = MockDataFactory.randomUuid()
        val sortId5 = MockDataFactory.randomUuid()
        val sort1 = StubSort(relationKey = relationKey1, id = sortId1)
        val sort2 = StubSort(relationKey = relationKey2, id = sortId2)
        val sort3 = StubSort(relationKey = relationKey3, id = sortId3)
        val sort4 = StubSort(relationKey = relationKey4, id = sortId4)
        val sort5 = StubSort(relationKey = relationKey5, id = sortId5)

        val viewer1 = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = listOf(sort1, sort2, sort3, sort4)
        )
        val viewer2 = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.GRID,
            sorts = listOf(sort1)
        )
        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer1, viewer2)
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer1.id,
            sortUpdates = listOf(
                Event.Command.DataView.UpdateView.DVSortUpdate.Move(
                    afterId = sortId3,
                    ids = listOf(sortId1, sortId2)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Add(
                    afterId = sortId3,
                    sorts = listOf(sort5)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Update(
                    id = sortId2,
                    sort = DVSort(sort2.id, relationKey2, Block.Content.DataView.Sort.Type.DESC)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Remove(
                    ids = listOf(sortId1)
                )
            ),
            filterUpdates = listOf(),
            relationUpdates = listOf(),
            fields = null
        )

        val eventOpen = Event.Command.ShowObject(
            context = context, root = context, blocks = blocks, details = details
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateUpdateView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedSorts = listOf(
            DVSort(
                id = sortId3,
                relationKey = relationKey3,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            DVSort(
                id = sortId5,
                relationKey = relationKey5,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            DVSort(
                id = sortId2,
                relationKey = relationKey2,
                type = Block.Content.DataView.Sort.Type.DESC
            ),
            DVSort(
                id = sortId4,
                relationKey = relationKey4,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewer1.id,
                        name = viewer1.name,
                        type = viewer1.type,
                        viewerRelations = viewer1.viewerRelations,
                        sorts = expectedSorts,
                        filters = viewer1.filters
                    ),
                    Block.Content.DataView.Viewer(
                        id = viewer2.id,
                        name = viewer2.name,
                        type = Block.Content.DataView.Viewer.Type.GRID,
                        viewerRelations = viewer2.viewerRelations,
                        sorts = listOf(sort1),
                        filters = listOf()
                    )
                ),
                targetObjectId = (dataView.content as Block.Content.DataView).targetObjectId
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView), details = details.details
        )

        assertEquals(expected, stateUpdateView)
    }

    @Test
    fun `when getting add, move, update, remove filters events, should proper update viewer`() = runTest {

        val title = StubTitle()

        val filterId1 = MockDataFactory.randomUuid()
        val filterId2 = MockDataFactory.randomUuid()
        val filterId3 = MockDataFactory.randomUuid()
        val filterId4 = MockDataFactory.randomUuid()
        val filterId5 = MockDataFactory.randomUuid()
        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relationKey4 = MockDataFactory.randomUuid()
        val relationKey5 = MockDataFactory.randomUuid()
        val filter1 = StubFilter(relationKey = relationKey1, id = filterId1)
        val filter2 = StubFilter(relationKey = relationKey2, id = filterId2)
        val filter3 = StubFilter(relationKey = relationKey3, id = filterId3)
        val filter4 = StubFilter(relationKey = relationKey4, id = filterId4)
        val filter5 = StubFilter(relationKey = relationKey5, id = filterId5)

        val viewer1 = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.BOARD,
            filters = listOf(filter1, filter2, filter3, filter4)
        )
        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer1)
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val filter2Value = MockDataFactory.randomString()
        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer1.id,
            sortUpdates = listOf(),
            filterUpdates = listOf(
                Event.Command.DataView.UpdateView.DVFilterUpdate.Move(
                    afterId = filterId3,
                    ids = listOf(filterId1, filterId2)
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Add(
                    afterId = filterId3,
                    filters = listOf(filter5)
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Update(
                    id = filterId2,
                    filter = filter2.copy(
                        value = filter2Value
                    )
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Remove(
                    ids = listOf(filterId1)
                )
            ),
            relationUpdates = listOf(),
            fields = null
        )

        val eventOpen = Event.Command.ShowObject(
            context = context, root = context, blocks = blocks, details = details
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateUpdateView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedFilters = listOf(
            filter3,
            filter5,
            filter2.copy(
                value = filter2Value
            ),
            filter4
        )

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewer1.id,
                        name = viewer1.name,
                        type = viewer1.type,
                        viewerRelations = viewer1.viewerRelations,
                        sorts = viewer1.sorts,
                        filters = expectedFilters
                    )
                ),
                targetObjectId = (dataView.content as Block.Content.DataView).targetObjectId
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView),
            details = details.details
        )

        assertEquals(expected, stateUpdateView)
    }

    @Test
    fun `when getting add, move, update, remove relations events, should proper update viewer`() = runTest {

        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relationKey4 = MockDataFactory.randomUuid()
        val relationKey5 = MockDataFactory.randomUuid()
        val relation1 = StubDataViewViewRelation(key = relationKey1)
        val relation2 = StubDataViewViewRelation(key = relationKey2)
        val relation3 = StubDataViewViewRelation(key = relationKey3)
        val relation4 = StubDataViewViewRelation(key = relationKey4)
        val relation5 = StubDataViewViewRelation(key = relationKey5)

        val viewer1 = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.BOARD,
            viewerRelations = listOf(relation1, relation2, relation3, relation4)
        )
        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer1)
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val relation2IsVisible = !relation2.isVisible
        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer1.id,
            sortUpdates = listOf(),
            filterUpdates = listOf(),
            relationUpdates = listOf(
                Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Move(
                    afterId = relationKey3,
                    ids = listOf(relationKey1, relationKey2)
                ),
                Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Add(
                    afterId = relationKey3,
                    relations = listOf(relation5)
                ),
                Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Update(
                    id = relationKey2,
                    relation = relation2.copy(
                        isVisible = relation2IsVisible
                    )
                ),
                Event.Command.DataView.UpdateView.DVViewerRelationUpdate.Remove(
                    ids = listOf(relationKey1)
                )
            ),
            fields = null
        )

        val eventOpen = Event.Command.ShowObject(
            context = context, root = context, blocks = blocks, details = details
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateUpdateView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedRelations = listOf(
            relation3,
            relation5,
            relation2.copy(
                isVisible = relation2IsVisible
            ),
            relation4
        )

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewer1.id,
                        name = viewer1.name,
                        type = viewer1.type,
                        viewerRelations = expectedRelations,
                        sorts = viewer1.sorts,
                        filters = viewer1.filters
                    )
                ),
                targetObjectId = (dataView.content as Block.Content.DataView).targetObjectId
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView), details = details.details
        )

        assertEquals(expected, stateUpdateView)
    }

    @Test
    fun `should update fields for viewer, with new fields`() = runTest {
        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relation1 = StubDataViewViewRelation(key = relationKey1)
        val relation2 = StubDataViewViewRelation(key = relationKey2)
        val filter1 = StubFilter(relationKey = relationKey1)
        val filter2 = StubFilter(relationKey = relationKey2)
        val sort1 = StubSort(relationKey = relationKey1)
        val sort2 = StubSort(relationKey = relationKey2)

        val viewer = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = listOf(relation1, relation2),
            filters = listOf(filter1, filter2),
            sorts = listOf(sort1, sort2),
            name = MockDataFactory.randomString(),
            cardSize = Block.Content.DataView.Viewer.Size.MEDIUM,
            coverFit = true,
            coverRelationKey = relationKey1,
            hideIcon = false
        )

        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer),
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val newViewerName = MockDataFactory.randomString()
        val newCardSize = Block.Content.DataView.Viewer.Size.SMALL
        val newCoverFit = false

        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer.id,
            sortUpdates = listOf(),
            filterUpdates = listOf(),
            relationUpdates = listOf(),
            fields = Event.Command.DataView.UpdateView.DVViewerFields(
                name = newViewerName,
                cardSize = newCardSize,
                coverFit = newCoverFit,
                coverRelationKey = relationKey1,
                hideIcon = false,
                type = Block.Content.DataView.Viewer.Type.GRID
            )
        )

        val eventOpen = Event.Command.ShowObject(
            context = context, root = context, blocks = blocks, details = details
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateUpdateView = reducer.reduce(
            state = stateOpen, events = listOf(event)
        ).state

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewer.id,
                        type = viewer.type,
                        viewerRelations = viewer.viewerRelations,
                        sorts = viewer.sorts,
                        filters = viewer.filters,
                        name = newViewerName,
                        cardSize = newCardSize,
                        coverFit = newCoverFit,
                        coverRelationKey = relationKey1
                    )
                ),
                targetObjectId = (dataView.content as Block.Content.DataView).targetObjectId
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView), details = details.details
        )

        assertEquals(expected, stateUpdateView)
    }

    @Test
    fun `when relation deleted from dataview, should delete it from relationLinks`() {
        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relation1 = StubDataViewViewRelation(key = relationKey1)
        val relation2 = StubDataViewViewRelation(key = relationKey2)
        val relation3 = StubDataViewViewRelation(key = relationKey3)

        val relationLink1 = StubRelationLink(key = relationKey1)
        val relationLink2 = StubRelationLink(key = relationKey2)
        val relationLink3 = StubRelationLink(key = relationKey3)

        val viewer = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = listOf(relation1, relation2, relation3),
            name = MockDataFactory.randomString()
        )

        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer),
            relationLinks = listOf(relationLink1, relationLink2, relationLink3)
        )

        val blocks = listOf(title, dataView)

        // TESTING

        val eventDeleteRelation = Event.Command.DataView.DeleteRelation(
            context = context,
            dv = dataView.id,
            keys = listOf(relationKey2)
        )

        val eventOpen = Event.Command.ShowObject(
            context = context, root = context, blocks = blocks, details = details
        )

        val stateOpen = reducer.reduce(
            state = ObjectState.Init, events = listOf(eventOpen)
        ).state

        val stateDeleteRelation = reducer.reduce(
            state = stateOpen, events = listOf(eventDeleteRelation)
        ).state

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                viewers = listOf(
                    Block.Content.DataView.Viewer(
                        id = viewer.id,
                        type = viewer.type,
                        viewerRelations = viewer.viewerRelations,
                        sorts = viewer.sorts,
                        filters = viewer.filters,
                        name = viewer.name,
                        coverRelationKey = viewer.coverRelationKey
                    )
                ),
                relationLinks = listOf(relationLink1, relationLink3),
                targetObjectId = (dataView.content as Block.Content.DataView).targetObjectId
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectState.DataView.Set(
            blocks = listOf(title, expectedDataView), details = details.details
        )

        assertEquals(expected, stateDeleteRelation)
    }

    val details = Block.Details(
        details = mapOf(
            context to Block.Fields(
                mapOf(Relations.ID to context, Relations.LAYOUT to 3.0)
            )
        )
    )
}