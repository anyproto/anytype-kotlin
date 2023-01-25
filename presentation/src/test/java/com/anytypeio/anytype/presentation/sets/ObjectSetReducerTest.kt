package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubFilter
import com.anytypeio.anytype.core_models.StubSort
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class ObjectSetReducerTest {

    private lateinit var reducer: ObjectSetReducer

    @Before
    fun reduce() {
        reducer = ObjectSetReducer()
    }

    @Test
    fun `should update sorts in viewer`() {

        val context = MockDataFactory.randomUuid()

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

        val dataViewRelations = listOf(
            Relation(
                key = viewerRelations[0].key,
                name = "Name",
                format = Relation.Format.LONG_TEXT,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
            ),
            Relation(
                key = viewerRelations[1].key,
                name = "Author",
                format = Relation.Format.LONG_TEXT,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
            ),
            Relation(
                key = viewerRelations[2].key,
                name = "Last modified date",
                format = Relation.Format.DATE,
                isReadOnly = true,
                isHidden = true,
                isMulti = false,
                source = Relation.Source.DERIVED,
                selections = listOf(),
                defaultValue = null
            ),
            Relation(
                key = viewerRelations[3].key,
                name = "Year",
                format = Relation.Format.NUMBER,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
            ),
            Relation(
                key = viewerRelations[4].key,
                name = "Country",
                format = Relation.Format.LONG_TEXT,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
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
                sources = listOf("source://1"),
                viewers = listOf(viewerGrid),
                relations = dataViewRelations
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        val objectSet = ObjectSet(blocks = blocks)

        // TESTING

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

        val result = reducer.reduce(state = objectSet, events = listOf(event))

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                sources = (dataView.content as Block.Content.DataView).sources,
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
                ),
                relations = dataViewRelations
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectSetReducer.Transformation(
            state = ObjectSet(blocks = listOf(title, expectedDataView)),
            effects = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should update list viewer`() {

        val context = MockDataFactory.randomUuid()

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

        val dataViewRelations = listOf(
            Relation(
                key = viewerRelations[0].key,
                name = "Name",
                format = Relation.Format.LONG_TEXT,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
            ),
            Relation(
                key = viewerRelations[1].key,
                name = "Author",
                format = Relation.Format.LONG_TEXT,
                isReadOnly = true,
                isHidden = false,
                isMulti = false,
                source = Relation.Source.DETAILS,
                selections = listOf(),
                defaultValue = null
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
                sources = listOf("source://1"),
                viewers = listOf(viewerGrid, viewerList),
                relations = dataViewRelations
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        val objectSet = ObjectSet(blocks = blocks)

        // TESTING

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

        val result = reducer.reduce(state = objectSet, events = listOf(event))

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                sources = (dataView.content as Block.Content.DataView).sources,
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
                ),
                relations = dataViewRelations
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectSetReducer.Transformation(
            state = ObjectSet(blocks = listOf(title, expectedDataView)),
            effects = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `when getting add, move, update, remove sorts events, should proper update viewer`() {

        val context = MockDataFactory.randomUuid()
        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relationKey4 = MockDataFactory.randomUuid()
        val relationKey5 = MockDataFactory.randomUuid()
        val sort1 = StubSort(relationKey = relationKey1)
        val sort2 = StubSort(relationKey = relationKey2)
        val sort3 = StubSort(relationKey = relationKey3)
        val sort4 = StubSort(relationKey = relationKey4)
        val sort5 = StubSort(relationKey = relationKey5)

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
            views = listOf(viewer1, viewer2),
            sources = listOf(MockDataFactory.randomString())
        )

        val blocks = listOf(title, dataView)

        val objectSet = ObjectSet(blocks = blocks)

        // TESTING

        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer1.id,
            sortUpdates = listOf(
                Event.Command.DataView.UpdateView.DVSortUpdate.Move(
                    afterId = relationKey3,
                    ids = listOf(relationKey1, relationKey2)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Add(
                    afterId = relationKey3,
                    sorts = listOf(sort5)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Update(
                    id = relationKey2,
                    sort = DVSort(relationKey2, Block.Content.DataView.Sort.Type.DESC)
                ),
                Event.Command.DataView.UpdateView.DVSortUpdate.Remove(
                    ids = listOf(relationKey1)
                )
            ),
            filterUpdates = listOf(),
            relationUpdates = listOf()
        )

        val result = reducer.reduce(state = objectSet, events = listOf(event))

        val expectedSorts = listOf(
            DVSort(
                relationKey = relationKey3,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            DVSort(
                relationKey = relationKey5,
                type = Block.Content.DataView.Sort.Type.ASC
            ),
            DVSort(
                relationKey = relationKey2,
                type = Block.Content.DataView.Sort.Type.DESC
            ),
            DVSort(
                relationKey = relationKey4,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                sources = (dataView.content as Block.Content.DataView).sources,
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
                relations = listOf()
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectSetReducer.Transformation(
            state = ObjectSet(blocks = listOf(title, expectedDataView)),
            effects = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `when getting add, move, update, remove filters events, should proper update viewer`() {

        val context = MockDataFactory.randomUuid()
        val title = StubTitle()

        val relationKey1 = MockDataFactory.randomUuid()
        val relationKey2 = MockDataFactory.randomUuid()
        val relationKey3 = MockDataFactory.randomUuid()
        val relationKey4 = MockDataFactory.randomUuid()
        val relationKey5 = MockDataFactory.randomUuid()
        val filter1 = StubFilter(relationKey = relationKey1)
        val filter2 = StubFilter(relationKey = relationKey2)
        val filter3 = StubFilter(relationKey = relationKey3)
        val filter4 = StubFilter(relationKey = relationKey4)
        val filter5 = StubFilter(relationKey = relationKey5)

        val viewer1 = StubDataViewView(
            type = Block.Content.DataView.Viewer.Type.BOARD,
            filters = listOf(filter1, filter2, filter3, filter4)
        )
        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(viewer1),
            sources = listOf(MockDataFactory.randomString())
        )

        val blocks = listOf(title, dataView)

        val objectSet = ObjectSet(blocks = blocks)

        // TESTING

        val filter2Value = MockDataFactory.randomString()
        val event = Event.Command.DataView.UpdateView(
            context = context,
            block = dataView.id,
            viewerId = viewer1.id,
            sortUpdates = listOf(),
            filterUpdates = listOf(
                Event.Command.DataView.UpdateView.DVFilterUpdate.Move(
                    afterId = relationKey3,
                    ids = listOf(relationKey1, relationKey2)
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Add(
                    afterId = relationKey3,
                    filters = listOf(filter5)
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Update(
                    id = relationKey2,
                    filter = filter2.copy(
                        value = filter2Value
                    )
                ),
                Event.Command.DataView.UpdateView.DVFilterUpdate.Remove(
                    ids = listOf(relationKey1)
                )
            ),
            relationUpdates = listOf()
        )

        val result = reducer.reduce(state = objectSet, events = listOf(event))

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
                sources = (dataView.content as Block.Content.DataView).sources,
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
                relations = listOf()
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectSetReducer.Transformation(
            state = ObjectSet(blocks = listOf(title, expectedDataView)),
            effects = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `when getting add, move, update, remove relations events, should proper update viewer`() {

        val context = MockDataFactory.randomUuid()
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
            views = listOf(viewer1),
            sources = listOf(MockDataFactory.randomString())
        )

        val blocks = listOf(title, dataView)

        val objectSet = ObjectSet(blocks = blocks)

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
            )
        )

        val result = reducer.reduce(state = objectSet, events = listOf(event))

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
                sources = (dataView.content as Block.Content.DataView).sources,
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
                relations = listOf()
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val expected = ObjectSetReducer.Transformation(
            state = ObjectSet(blocks = listOf(title, expectedDataView)),
            effects = emptyList()
        )

        assertEquals(expected, result)
    }
}