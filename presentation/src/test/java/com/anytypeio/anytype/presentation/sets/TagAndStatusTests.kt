package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationOptionObject
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.presentation.mapper.toViewerColumns
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class TagAndStatusTests {

    private val defaultSpace = MockDataFactory.randomUuid()

    @Mock
    lateinit var gateway: Gateway

    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var logger: Logger


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger)
    }

    @Test
    fun `should create row with text and tag cells`() = runTest {

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
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Tag1",
                color = "000"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Tag2",
                color = "111"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Tag3",
                color = "222"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Tag4",
                color = "333"
            )
        )

        val recordId = MockDataFactory.randomUuid()
        val records = mapOf<String, Any?>(
            ObjectSetConfig.ID_KEY to recordId,
            Relations.SPACE_ID to defaultSpace,
            ObjectSetConfig.TYPE_KEY to "Type111",
            viewerRelations[0].key to "Title4",
            viewerRelations[1].key to listOf(selOptions[1].id, selOptions[2].id)
        )

        val dataViewRelations = listOf(
            ObjectWrapper.Relation(
                mapOf(
                    Relations.RELATION_KEY to viewerRelations[0].key,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.NAME to "name",
                    Relations.RELATION_FORMAT to Relation.Format.LONG_TEXT.code.toDouble(),
                    Relations.IS_READ_ONLY to true,
                    Relations.IS_HIDDEN to false,
                    Relations.SOURCE to Relation.Source.DETAILS.name
                )
            ),
            ObjectWrapper.Relation(
                mapOf(
                    Relations.RELATION_KEY to viewerRelations[1].key,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.NAME to "Tags",
                    Relations.RELATION_FORMAT to RelationFormat.TAG.code.toDouble(),
                    Relations.IS_READ_ONLY to true,
                    Relations.IS_HIDDEN to false,
                    Relations.SOURCE to Relation.Source.DETAILS.name
                )
            )
        )

        val store = DefaultObjectStore()

        store.merge(
            objects = listOf(ObjectWrapper.Basic(records)),
            dependencies = selOptions.map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        //TESTING

        val columns: List<ColumnView> = viewerRelations.toViewerColumns(
            relations = dataViewRelations,
            filterBy = listOf()
        )

        val result = columns.buildGridRow(
            obj = ObjectWrapper.Basic(records),
            builder = UrlBuilder(gateway),
            showIcon = false,
            store = store,
            fieldParser = fieldParser
        )

        val expected = Viewer.GridView.Row(
            id = recordId,
            name = "",
            type = "Type111",
            showIcon = false,
            objectIcon = ObjectIcon.Empty.Page,
            cells = listOf(
                CellView.Description(
                    id = recordId,
                    relationKey = viewerRelations[0].key,
                    text = "Title4"
                ),
                CellView.Tag(
                    id = recordId,
                    relationKey = viewerRelations[1].key,
                    tags = listOf(
                        TagView(
                            id = selOptions[1].id,
                            tag = selOptions[1].name.orEmpty(),
                            color = selOptions[1].color
                        ),
                        TagView(
                            id = selOptions[2].id,
                            tag = selOptions[2].name.orEmpty(),
                            color = selOptions[2].color
                        )
                    ),
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should create row with text and status cells`() = runTest {

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
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Status1",
                color = "000"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Status2",
                color = "111"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Status3",
                color = "222"
            ),
            StubRelationOptionObject(
                id = MockDataFactory.randomUuid(),
                space = defaultSpace,
                text = "Status4",
                color = "333"
            )
        )

        val recordId = MockDataFactory.randomUuid()
        val records = mapOf<String, Any?>(
            ObjectSetConfig.ID_KEY to recordId,
            Relations.SPACE_ID to defaultSpace,
            ObjectSetConfig.TYPE_KEY to "Type111",
            viewerRelations[0].key to "Title4",
            viewerRelations[1].key to listOf(selOptions[2].id)
        )

        val dataViewRelations = listOf(
            ObjectWrapper.Relation(
                mapOf(
                    Relations.RELATION_KEY to viewerRelations[0].key,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.NAME to "name",
                    Relations.RELATION_FORMAT to Relation.Format.LONG_TEXT.code.toDouble(),
                    Relations.IS_READ_ONLY to true,
                    Relations.IS_HIDDEN to false,
                    Relations.SOURCE to Relation.Source.DETAILS.name
                )
            ),
            ObjectWrapper.Relation(
                mapOf(
                    Relations.RELATION_KEY to viewerRelations[1].key,
                    Relations.SPACE_ID to defaultSpace,
                    Relations.NAME to "Status",
                    Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble(),
                    Relations.IS_READ_ONLY to true,
                    Relations.IS_HIDDEN to false,
                    Relations.SOURCE to Relation.Source.DETAILS.name
                )
            )
        )

        val store = DefaultObjectStore()

        store.merge(
            objects = listOf(ObjectWrapper.Basic(records)),
            dependencies = selOptions.map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        //TESTING

        val columns: List<ColumnView> = viewerRelations.toViewerColumns(
            relations = dataViewRelations,
            filterBy = listOf()
        )

        val result = columns.buildGridRow(
            obj = ObjectWrapper.Basic(records),
            builder = UrlBuilder(gateway),
            showIcon = false,
            store = store,
            fieldParser = fieldParser
        )

        val expected = Viewer.GridView.Row(
            id = recordId,
            name = "",
            type = "Type111",
            showIcon = false,
            objectIcon = ObjectIcon.Empty.Page,
            cells = listOf(
                CellView.Description(
                    id = recordId,
                    relationKey = viewerRelations[0].key,
                    text = "Title4",
                ),
                CellView.Status(
                    id = recordId,
                    relationKey = viewerRelations[1].key,
                    status = listOf(
                        StatusView(
                            id = selOptions[2].id,
                            status = selOptions[2].name.orEmpty(),
                            color = selOptions[2].color
                        )
                    ),
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}