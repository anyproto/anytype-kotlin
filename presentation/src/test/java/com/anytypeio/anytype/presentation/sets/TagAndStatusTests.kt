package com.anytypeio.anytype.presentation.sets

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.mapper.toViewerColumns
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class TagAndStatusTests {

    @Mock
    lateinit var gateway: Gateway

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should create row with text and tag cells`() {

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

        val selOptions = listOf(
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Tag1",
                color = "000"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Tag2",
                color = "111"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Tag3",
                color = "222"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Tag4",
                color = "333"
            )
        )

        val recordId = MockDataFactory.randomUuid()
        val records = mapOf<String, Any?>(
            ObjectSetConfig.ID_KEY to recordId,
            ObjectSetConfig.TYPE_KEY to "Type111",
            viewerRelations[0].key to "Title4",
            viewerRelations[1].key to listOf(selOptions[1].id, selOptions[2].id)
        )

        val dataViewRelations = listOf(
            Relation(
                key = viewerRelations[0].key,
                name = "name",
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
                name = "Tags",
                format = Relation.Format.TAG,
                isReadOnly = true,
                isHidden = false,
                isMulti = true,
                source = Relation.Source.DETAILS,
                selections = selOptions,
                defaultValue = null
            )
        )

        val viewerGrid = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = viewerRelations,
            sorts = listOf(),
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

        val blocks = listOf(dataView)

        val objectSet = ObjectSet(blocks = blocks)

        //TESTING

        val columns: List<ColumnView> = viewerRelations.toViewerColumns(
            relations = dataViewRelations,
            filterBy = listOf()
        )

        val result = columns.buildGridRow(
            record = records,
            relations = dataViewRelations,
            builder = UrlBuilder(gateway),
            details = emptyMap()
        )

        val expected = Viewer.GridView.Row(
            id = recordId,
            type = "Type111",
            cells = listOf(
                CellView.Description(
                    id = recordId,
                    key = viewerRelations[0].key,
                    text = "Title4"
                ),
                CellView.Tag(
                    id = recordId,
                    key = viewerRelations[1].key,
                    tags = listOf(
                        TagView(
                            id = selOptions[1].id,
                            tag = selOptions[1].text,
                            color = selOptions[1].color
                        ),
                        TagView(
                            id = selOptions[2].id,
                            tag = selOptions[2].text,
                            color = selOptions[2].color
                        )
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should create row with text and status cells`() {

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

        val selOptions = listOf(
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Status1",
                color = "000"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Status2",
                color = "111"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Status3",
                color = "222"
            ),
            Relation.Option(
                id = MockDataFactory.randomUuid(),
                text = "Status4",
                color = "333"
            )
        )

        val recordId = MockDataFactory.randomUuid()
        val records = mapOf<String, Any?>(
            ObjectSetConfig.ID_KEY to recordId,
            ObjectSetConfig.TYPE_KEY to "Type111",
            viewerRelations[0].key to "Title4",
            viewerRelations[1].key to listOf(selOptions[2].id, selOptions[3].id)
        )

        val dataViewRelations = listOf(
            Relation(
                key = viewerRelations[0].key,
                name = "name",
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
                name = "Tags",
                format = Relation.Format.STATUS,
                isReadOnly = true,
                isHidden = false,
                isMulti = true,
                source = Relation.Source.DETAILS,
                selections = selOptions,
                defaultValue = null
            )
        )

        val viewerGrid = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = viewerRelations,
            sorts = listOf(),
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

        val blocks = listOf(dataView)

        val objectSet = ObjectSet(blocks = blocks)

        //TESTING

        val columns: List<ColumnView> = viewerRelations.toViewerColumns(
            relations = dataViewRelations,
            filterBy = listOf()
        )

        val result = columns.buildGridRow(
            record = records,
            relations = dataViewRelations,
            builder = UrlBuilder(gateway),
            details = emptyMap()
        )

        val expected = Viewer.GridView.Row(
            id = recordId,
            type = "Type111",
            cells = listOf(
                CellView.Description(
                    id = recordId,
                    key = viewerRelations[0].key,
                    text = "Title4"
                ),
                CellView.Status(
                    id = recordId,
                    key = viewerRelations[1].key,
                    status = listOf(
                        StatusView(
                            id = selOptions[2].id,
                            status = selOptions[2].text,
                            color = selOptions[2].color
                        ),
                        StatusView(
                            id = selOptions[3].id,
                            status = selOptions[3].text,
                            color = selOptions[3].color
                        )
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}