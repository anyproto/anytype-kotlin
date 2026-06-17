package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubRelationOptionObject
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class BoardViewMapperTest {

    private val defaultSpace = MockDataFactory.randomUuid()

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    private lateinit var fieldParser: FieldParser

    private val statusRelationKey = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        whenever(stringResourceProvider.getUntitledObjectTitle()).thenReturn("Untitled")
    }

    @Test
    fun `should group records into columns by status with resolved labels and colors`() = runTest {

        whenever(stringResourceProvider.getKanbanEmptyColumnTitle("Status")).thenReturn("No Status")

        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(),
            space = defaultSpace,
            text = "To do",
            color = "red"
        )
        val doing = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(),
            space = defaultSpace,
            text = "Doing",
            color = "blue"
        )

        val objA = record(id = "A", name = "Task A", status = listOf(todo.id))
        val objB = record(id = "B", name = "Task B", status = listOf(todo.id))
        val objC = record(id = "C", name = "Task C", status = listOf(doing.id))
        val objD = record(id = "D", name = "Task D", status = null)

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(objA, objB, objC, objD).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(todo, doing).map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Status")))
        }

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(key = statusRelationKey, isVisible = true)
            ),
            groupRelationKey = statusRelationKey
        )

        val columns = viewer.buildBoardViews(
            objectIds = listOf("A", "B", "C", "D"),
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrderIds = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider
        )

        assertEquals(3, columns.size)

        val first = columns[0]
        assertEquals(todo.id, first.id)
        assertEquals("To do", first.label)
        assertEquals("red", first.color)
        assertEquals(listOf("A", "B"), first.cards.map { it.objectId })

        val second = columns[1]
        assertEquals(doing.id, second.id)
        assertEquals("Doing", second.label)
        assertEquals("blue", second.color)
        assertEquals(listOf("C"), second.cards.map { it.objectId })

        val empty = columns[2]
        assertEquals(BOARD_EMPTY_GROUP_ID, empty.id)
        assertEquals("No Status", empty.label)
        assertEquals(null, empty.color)
        assertEquals(listOf("D"), empty.cards.map { it.objectId })
    }

    @Test
    fun `should order cards within a column by object order`() = runTest {

        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(),
            space = defaultSpace,
            text = "To do",
            color = "red"
        )

        val objA = record(id = "A", name = "Task A", status = listOf(todo.id))
        val objB = record(id = "B", name = "Task B", status = listOf(todo.id))
        val objC = record(id = "C", name = "Task C", status = listOf(todo.id))

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(objA, objB, objC).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(ObjectWrapper.Basic(todo.map)),
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Status")))
        }

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(key = statusRelationKey, isVisible = true)
            ),
            groupRelationKey = statusRelationKey
        )

        val columns = viewer.buildBoardViews(
            objectIds = listOf("A", "B", "C"),
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrderIds = listOf("C", "A", "B"),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider
        )

        assertEquals(1, columns.size)
        assertEquals(listOf("C", "A", "B"), columns[0].cards.map { it.objectId })
    }

    private fun record(id: String, name: String, status: List<String>?): Map<String, Any?> = buildMap {
        put(ObjectSetConfig.ID_KEY, id)
        put(Relations.SPACE_ID, defaultSpace)
        put(Relations.NAME, name)
        if (status != null) put(statusRelationKey, status)
    }

    private fun statusRelation(name: String): ObjectWrapper.Relation = ObjectWrapper.Relation(
        mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to statusRelationKey,
            Relations.SPACE_ID to defaultSpace,
            Relations.NAME to name,
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        )
    )

    private fun statusRelationWrapper(): ObjectWrapper.Relation = ObjectWrapper.Relation(
        mapOf(
            Relations.RELATION_KEY to statusRelationKey,
            Relations.SPACE_ID to defaultSpace,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble(),
            Relations.IS_READ_ONLY to true,
            Relations.IS_HIDDEN to false,
            Relations.SOURCE to Relation.Source.DETAILS.name
        )
    )
}
