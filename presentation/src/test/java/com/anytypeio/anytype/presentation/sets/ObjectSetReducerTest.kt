package com.anytypeio.anytype.presentation.sets

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Event
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class ObjectSetReducerTest {

    lateinit var reducer: ObjectSetReducer

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
                source = "source://1",
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
            ),
            offset = 10,
            limit = 10
        )

        val result = reducer.reduce(state = objectSet, events = listOf(event))

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                source = (dataView.content as Block.Content.DataView).source,
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

        val expected =
            ObjectSetReducer.Transformation(ObjectSet(blocks = listOf(title, expectedDataView)))

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
                source = "source://1",
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
            ),
            offset = 10,
            limit = 10
        )

        val result = reducer.reduce(state = objectSet, events = listOf(event))

        val expectedDataView = Block(
            id = dataView.id,
            content = Block.Content.DataView(
                source = (dataView.content as Block.Content.DataView).source,
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

        val expected =
            ObjectSetReducer.Transformation(ObjectSet(blocks = listOf(title, expectedDataView)))

        assertEquals(expected, result)
    }
}