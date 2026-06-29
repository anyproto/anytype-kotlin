package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.GroupOrder
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ViewGroup
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
        whenever(stringResourceProvider.getKanbanEmptyColumnTitle()).thenReturn("No value")
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
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = listOf(
                ObjectOrder(view = "view-1", group = todo.id, ids = listOf("C", "A", "B"))
            ),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(todo.id to ObjectWrapper.Option(todo.map)),
            groups = listOf(DataViewGroup(id = todo.id, value = DataViewGroup.Value.Status(todo.id))),
            recordsByColumn = mapOf(todo.id to listOf("A", "B", "C"))
        )

        assertEquals(2, columns.size)
        assertEquals(BOARD_EMPTY_GROUP_ID, columns[0].id)
        assertEquals(0, columns[0].cards.size)
        assertEquals(listOf("C", "A", "B"), columns[1].cards.map { it.objectId })
    }

    @Test
    fun `should show options without records as empty columns`() = runTest {

        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(),
            space = defaultSpace,
            text = "To do",
            color = "red"
        )
        val done = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(),
            space = defaultSpace,
            text = "Done",
            color = "blue"
        )

        val objA = record(id = "A", name = "Task A", status = listOf(todo.id))

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(ObjectWrapper.Basic(objA)),
            dependencies = listOf(todo, done).map { ObjectWrapper.Basic(it.map) },
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
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(
                todo.id to ObjectWrapper.Option(todo.map),
                done.id to ObjectWrapper.Option(done.map)
            ),
            groups = listOf(
                DataViewGroup(id = todo.id, value = DataViewGroup.Value.Status(todo.id)),
                DataViewGroup(id = done.id, value = DataViewGroup.Value.Status(done.id))
            ),
            // Only "To do" has a record; "Done" stays an empty column.
            recordsByColumn = mapOf(todo.id to listOf("A"))
        )

        // "No value" first, then groups in backend order (To do has the record, Done is empty).
        assertEquals(3, columns.size)
        assertEquals(BOARD_EMPTY_GROUP_ID, columns[0].id)
        assertEquals(todo.id, columns[1].id)
        assertEquals(listOf("A"), columns[1].cards.map { it.objectId })
        assertEquals(done.id, columns[2].id)
        assertEquals("Done", columns[2].label)
        assertEquals("blue", columns[2].color)
        assertEquals(0, columns[2].cards.size)
    }

    @Test
    fun `should order and hide columns by saved group order`() = runTest {

        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "To do", color = "red"
        )
        val done = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "Done", color = "blue"
        )
        val blocked = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "Blocked", color = "grey"
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                record(id = "T", name = "Task T", status = listOf(todo.id)),
                record(id = "D", name = "Task D", status = listOf(done.id)),
                record(id = "B", name = "Task B", status = listOf(blocked.id))
            ).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(todo, done, blocked).map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Status")))
        }

        val viewer = DVViewer(
            id = "view-1",
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
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(
                todo.id to ObjectWrapper.Option(todo.map),
                done.id to ObjectWrapper.Option(done.map),
                blocked.id to ObjectWrapper.Option(blocked.map)
            ),
            groups = listOf(
                DataViewGroup(id = todo.id, value = DataViewGroup.Value.Status(todo.id)),
                DataViewGroup(id = done.id, value = DataViewGroup.Value.Status(done.id)),
                DataViewGroup(id = blocked.id, value = DataViewGroup.Value.Status(blocked.id))
            ),
            groupOrder = GroupOrder(
                viewId = "view-1",
                // List order deliberately differs from the index field: ordering must
                // follow ViewGroup.index (done=0, todo=1), not list position.
                viewGroups = listOf(
                    ViewGroup(groupId = todo.id, index = 1, isHidden = false, backgroundColor = ""),
                    ViewGroup(groupId = done.id, index = 0, isHidden = false, backgroundColor = ""),
                    ViewGroup(groupId = blocked.id, index = 2, isHidden = true, backgroundColor = "")
                )
            )
        )

        // "Blocked" is hidden -> dropped; saved index puts Done (0) before To do (1);
        // the "No value" column is not in the saved order -> appended last.
        assertEquals(listOf(done.id, todo.id, BOARD_EMPTY_GROUP_ID), columns.map { it.id })
    }

    @Test
    fun `should build columns from backend groups and assign records by value`() = runTest {

        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "To do", color = "red"
        )
        val done = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "Done", color = "blue"
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                record(id = "A", name = "Task A", status = listOf(todo.id)),
                record(id = "B", name = "Task B", status = listOf(done.id)),
                record(id = "C", name = "Task C", status = null)
            ).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(todo, done).map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Status")))
        }

        val viewer = DVViewer(
            id = "view-1",
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(key = statusRelationKey, isVisible = true)
            ),
            groupRelationKey = statusRelationKey
        )

        val groups = listOf(
            DataViewGroup(id = "empty", value = DataViewGroup.Value.Empty),
            DataViewGroup(id = todo.id, value = DataViewGroup.Value.Status(todo.id)),
            DataViewGroup(id = done.id, value = DataViewGroup.Value.Status(done.id))
        )

        val columns = viewer.buildBoardViews(
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(
                todo.id to ObjectWrapper.Option(todo.map),
                done.id to ObjectWrapper.Option(done.map)
            ),
            groups = groups,
            recordsByColumn = mapOf(
                "empty" to listOf("C"),
                todo.id to listOf("A"),
                done.id to listOf("B")
            )
        )

        // Status boards keep the backend group order: "No value" first, then the option
        // groups in the order the backend returned them (no alphabetical re-sort).
        assertEquals(3, columns.size)
        assertEquals("empty", columns[0].id)
        assertEquals("No value", columns[0].label)
        assertEquals(listOf("C"), columns[0].cards.map { it.objectId })
        assertEquals(todo.id, columns[1].id)
        assertEquals("To do", columns[1].label)
        assertEquals(listOf("A"), columns[1].cards.map { it.objectId })
        assertEquals(done.id, columns[2].id)
        assertEquals("Done", columns[2].label)
        assertEquals(listOf("B"), columns[2].cards.map { it.objectId })
    }

    @Test
    fun `should order columns by default with combos before single tags then alphabetically`() = runTest {

        val tag1 = StubRelationOptionObject(
            id = "opt-tag1", space = defaultSpace, text = "tag1", color = "orange"
        )
        val tag2 = StubRelationOptionObject(
            id = "opt-tag2", space = defaultSpace, text = "tag2", color = "teal"
        )
        val tag3 = StubRelationOptionObject(
            id = "opt-tag3", space = defaultSpace, text = "tag3", color = "red"
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                record(id = "A", name = "Task A", status = listOf(tag1.id)),
                record(id = "B", name = "Task B", status = listOf(tag2.id, tag3.id))
            ).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(tag1, tag2, tag3).map { ObjectWrapper.Basic(it.map) },
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Tag")))
        }

        val viewer = DVViewer(
            id = "view-1",
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(key = statusRelationKey, isVisible = true)
            ),
            groupRelationKey = statusRelationKey
        )

        // Backend order is local-DB order (single tags shuffled, combo not first).
        val groups = listOf(
            DataViewGroup(id = tag3.id, value = DataViewGroup.Value.Tag(listOf(tag3.id))),
            DataViewGroup(id = "empty", value = DataViewGroup.Value.Tag(emptyList())),
            DataViewGroup(id = "combo", value = DataViewGroup.Value.Tag(listOf(tag2.id, tag3.id))),
            DataViewGroup(id = tag1.id, value = DataViewGroup.Value.Tag(listOf(tag1.id))),
            DataViewGroup(id = tag2.id, value = DataViewGroup.Value.Tag(listOf(tag2.id)))
        )

        val columns = viewer.buildBoardViews(
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(
                tag1.id to ObjectWrapper.Option(tag1.map),
                tag2.id to ObjectWrapper.Option(tag2.map),
                tag3.id to ObjectWrapper.Option(tag3.map)
            ),
            groups = groups
        )

        // No value first, then the multi-tag combo, then single tags alphabetically.
        assertEquals(
            listOf("empty", "combo", tag1.id, tag2.id, tag3.id),
            columns.map { it.id }
        )
        assertEquals("tag2, tag3", columns[1].label)
    }

    @Test
    fun `should not synthesize a No value column for checkbox boards`() = runTest {
        whenever(stringResourceProvider.getKanbanCheckboxGroupTitle(true)).thenReturn("Checked")
        whenever(stringResourceProvider.getKanbanCheckboxGroupTitle(false)).thenReturn("Unchecked")

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                checkboxRecord(id = "A", name = "Task A", checked = true),
                checkboxRecord(id = "B", name = "Task B", checked = false)
            ).map { ObjectWrapper.Basic(it) },
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply {
            merge(listOf(statusRelation(name = "Done")))
        }

        val viewer = DVViewer(
            id = "view-1",
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(key = statusRelationKey, isVisible = true)
            ),
            groupRelationKey = statusRelationKey
        )

        val groups = listOf(
            DataViewGroup(id = "true", value = DataViewGroup.Value.Checkbox(checked = true)),
            DataViewGroup(id = "false", value = DataViewGroup.Value.Checkbox(checked = false))
        )

        val columns = viewer.buildBoardViews(
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groups = groups,
            recordsByColumn = mapOf("true" to listOf("A"), "false" to listOf("B"))
        )

        // Checkbox boards have only Checked / Unchecked — no phantom "No value" column.
        assertEquals(listOf("true", "false"), columns.map { it.id })
        assertEquals(listOf("Checked", "Unchecked"), columns.map { it.label })
        assertEquals(listOf("A"), columns[0].cards.map { it.objectId })
        assertEquals(listOf("B"), columns[1].cards.map { it.objectId })
    }

    @Test
    fun `column count reflects the backend total even when fewer cards are loaded`() = runTest {
        val todo = StubRelationOptionObject(
            id = MockDataFactory.randomUuid(), space = defaultSpace, text = "To do", color = "red"
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(record(id = "A", name = "Task A", status = listOf(todo.id))).map { ObjectWrapper.Basic(it) },
            dependencies = listOf(ObjectWrapper.Basic(todo.map)),
            subscriptions = emptyList()
        )

        val storeOfRelations = DefaultStoreOfRelations().apply { merge(listOf(statusRelation(name = "Status"))) }

        val viewer = DVViewer(
            id = "view-1",
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
            relations = listOf(statusRelationWrapper()),
            urlBuilder = UrlBuilderImpl(gateway),
            objectStore = store,
            objectOrders = emptyList(),
            storeOfRelations = storeOfRelations,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            stringResourceProvider = stringResourceProvider,
            groupOptions = mapOf(todo.id to ObjectWrapper.Option(todo.map)),
            groups = listOf(DataViewGroup(id = todo.id, value = DataViewGroup.Value.Status(todo.id))),
            // Subscription loaded 1 card of a 120-card column.
            recordsByColumn = mapOf(todo.id to listOf("A")),
            countsByColumn = mapOf(todo.id to 120)
        )

        val column = columns.first { it.id == todo.id }
        assertEquals(1, column.cards.size)
        assertEquals(120, column.count)
    }

    private fun checkboxRecord(id: String, name: String, checked: Boolean): Map<String, Any?> = buildMap {
        put(ObjectSetConfig.ID_KEY, id)
        put(Relations.SPACE_ID, defaultSpace)
        put(Relations.NAME, name)
        put(statusRelationKey, checked)
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
