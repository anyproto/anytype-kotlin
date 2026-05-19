package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ViewGroup
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelationOptions
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelationOptions
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectRelationValueView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class KanbanViewMapperTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

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
    private lateinit var storeOfRelationOptions: StoreOfRelationOptions

    private val urlBuilder by lazy { UrlBuilderImpl(gateway) }

    private val untitledPlaceholder = "Untitled"
    private val ungroupedColumnName = "No Status"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        storeOfRelationOptions = DefaultStoreOfRelationOptions()
        doReturn(untitledPlaceholder).whenever(stringResourceProvider).getUntitledObjectTitle()
        doReturn(ungroupedColumnName).whenever(stringResourceProvider).getKanbanUngroupedColumnName(relationName = "Status")
    }

    private suspend fun seedOptions(vararg options: ObjectWrapper.Option) {
        storeOfRelationOptions.merge(options.toList())
    }

    private fun option(
        id: String,
        relationKey: String,
        name: String? = null,
        color: String? = null,
        isDeleted: Boolean = false
    ): ObjectWrapper.Option = ObjectWrapper.Option(
        buildMap {
            put(Relations.ID, id)
            put(Relations.RELATION_KEY, relationKey)
            if (name != null) put(Relations.NAME, name)
            if (color != null) put(Relations.RELATION_OPTION_COLOR, color)
            if (isDeleted) put(Relations.IS_DELETED, true)
        }
    )

    @Test
    fun `objects with status values are assigned to correct columns`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val option2Id = MockDataFactory.randomUuid()
        val obj1Id = MockDataFactory.randomUuid()
        val obj2Id = MockDataFactory.randomUuid()
        val obj3Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Object 1",
                    groupRelationKey to listOf(option1Id)
                )),
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj2Id,
                    Relations.NAME to "Object 2",
                    groupRelationKey to listOf(option1Id)
                )),
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj3Id,
                    Relations.NAME to "Object 3",
                    groupRelationKey to listOf(option2Id)
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey, name = "Done", color = "green"),
            option(id = option2Id, relationKey = groupRelationKey, name = "In Progress", color = "blue")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        // ObjectOrder controls only the WITHIN-COLUMN ordering, not column assignment.
        val objectOrders = listOf(
            ObjectOrder(view = viewerId, group = option1Id, ids = listOf(obj1Id, obj2Id))
        )

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(obj1Id, obj2Id, obj3Id),
            dataViewRelations = emptyList(),
            objectOrders = objectOrders,
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        // Two option columns + always-appended ungrouped column
        assertEquals(3, result.columns.size)
        val byGroup = result.columns.associateBy { it.groupId }
        val doneCol = byGroup.getValue(option1Id)
        assertEquals("Done", doneCol.name)
        assertEquals("green", doneCol.color)
        assertEquals(2, doneCol.cards.size)
        assertEquals(obj1Id, doneCol.cards[0].objectId)
        assertEquals(obj2Id, doneCol.cards[1].objectId)

        val inProgressCol = byGroup.getValue(option2Id)
        assertEquals("In Progress", inProgressCol.name)
        assertEquals(1, inProgressCol.cards.size)
        assertEquals(obj3Id, inProgressCol.cards[0].objectId)

        val ungrouped = byGroup.getValue(KANBAN_UNGROUPED_COLUMN_ID)
        assertEquals(ungroupedColumnName, ungrouped.name)
        assertTrue(ungrouped.cards.isEmpty())
        // Ungrouped is always last
        assertEquals(KANBAN_UNGROUPED_COLUMN_ID, result.columns.last().groupId)
    }

    @Test
    fun `objects not in any objectOrder appear in ungrouped column`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val obj1Id = MockDataFactory.randomUuid()
        val obj2Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Grouped",
                    groupRelationKey to listOf(option1Id)
                )),
                // No groupRelationKey value → falls into ungrouped
                ObjectWrapper.Basic(mapOf(Relations.ID to obj2Id, Relations.NAME to "Ungrouped"))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey, name = "Status A")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val objectOrders = emptyList<ObjectOrder>()

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(obj1Id, obj2Id),
            dataViewRelations = emptyList(),
            objectOrders = objectOrders,
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        // One option column + ungrouped
        assertEquals(2, result.columns.size)
        val ungroupedCol = result.columns.find { it.groupId == KANBAN_UNGROUPED_COLUMN_ID }
        assertNotNull(ungroupedCol)
        assertEquals(1, ungroupedCol.cards.size)
        assertEquals(obj2Id, ungroupedCol.cards[0].objectId)
    }

    @Test
    fun `option without name renders Untitled placeholder as column header`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey)
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = emptyList(),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val optionCol = result.columns.find { it.groupId == option1Id }
        assertNotNull(optionCol)
        // Fall back to Untitled placeholder, not the raw option UUID
        assertEquals(untitledPlaceholder, optionCol.name)
    }

    @Test
    fun `deleted options are skipped from column list`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val keptOptionId = MockDataFactory.randomUuid()
        val deletedOptionId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()

        seedOptions(
            option(id = keptOptionId, relationKey = groupRelationKey, name = "Active"),
            option(id = deletedOptionId, relationKey = groupRelationKey, name = "Removed", isDeleted = true)
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = emptyList(),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val groupIds = result.columns.map { it.groupId }.toSet()
        assertTrue(keptOptionId in groupIds, "Kept option should be a column")
        assertTrue(deletedOptionId !in groupIds, "Deleted option should be filtered out")
    }

    @Test
    fun `every option becomes a column even when it has no objects`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val emptyOptionId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()

        seedOptions(
            option(id = emptyOptionId, relationKey = groupRelationKey, name = "Empty Option")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = emptyList(),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val emptyCol = result.columns.find { it.groupId == emptyOptionId }
        assertNotNull(emptyCol)
        assertTrue(emptyCol.cards.isEmpty())
    }

    @Test
    fun `card relations include visible viewer relations excluding NAME but keeping group key`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val descriptionKey = MockDataFactory.randomString()
        val priorityKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val obj1Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = Relations.NAME,
                    isVisible = true
                ),
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = groupRelationKey,
                    isVisible = true
                ),
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = descriptionKey,
                    isVisible = true
                ),
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = priorityKey,
                    isVisible = false
                )
            ),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Task 1",
                    descriptionKey to "A short note",
                    priorityKey to "High",
                    groupRelationKey to listOf(option1Id)
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey, name = "Done")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val descriptionRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to descriptionKey,
            Relations.NAME to "Description",
            Relations.RELATION_FORMAT to Relation.Format.LONG_TEXT.code.toDouble()
        ))

        val priorityRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to priorityKey,
            Relations.NAME to "Priority",
            Relations.RELATION_FORMAT to Relation.Format.LONG_TEXT.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(obj1Id),
            dataViewRelations = listOf(groupRelation, descriptionRelation, priorityRelation),
            objectOrders = listOf(ObjectOrder(view = viewerId, group = option1Id, ids = listOf(obj1Id))),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val card = result.columns.find { it.groupId == option1Id }?.cards?.firstOrNull()
        assertNotNull(card)
        // NAME is rendered as title; hidden relations are dropped. Group relation IS kept
        // on the card so its chip can be tapped to change value (move between columns).
        val byKey = card.relations.associateBy { it.relationKey }
        assertTrue(descriptionKey in byKey, "Description should appear on card")
        assertTrue(groupRelationKey in byKey, "Group relation should appear on card as tap target")
        assertTrue(priorityKey !in byKey, "Hidden relation should not appear")
        assertTrue(Relations.NAME !in byKey, "NAME relation lives in title slot, not chips")
    }

    @Test
    fun `hideName flag is set when NAME relation is not visible in viewer relations`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val obj1Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = listOf(
                Block.Content.DataView.Viewer.ViewerRelation(
                    key = Relations.NAME,
                    isVisible = false
                )
            ),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Task",
                    groupRelationKey to listOf(option1Id)
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey, name = "Done")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(obj1Id),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val card = result.columns.find { it.groupId == option1Id }?.cards?.firstOrNull()
        assertNotNull(card)
        assertTrue(card.hideName, "hideName should be true when NAME relation is not visible")
    }

    @Test
    fun `tag value with multiple options places card in each matching column`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val option1Id = MockDataFactory.randomUuid()
        val option2Id = MockDataFactory.randomUuid()
        val sharedObjId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to sharedObjId,
                    Relations.NAME to "Shared",
                    // Multi-tag value: object belongs to BOTH options at once.
                    groupRelationKey to listOf(option1Id, option2Id)
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = option1Id, relationKey = groupRelationKey, name = "A"),
            option(id = option2Id, relationKey = groupRelationKey, name = "B")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.TAG.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(sharedObjId),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        val byGroup = result.columns.associateBy { it.groupId }
        assertEquals(1, byGroup.getValue(option1Id).cards.size, "Card must appear in option1 column")
        assertEquals(1, byGroup.getValue(option2Id).cards.size, "Card must appear in option2 column")
        // Ungrouped should be empty because the object DOES have group values.
        val ungrouped = byGroup.getValue(KANBAN_UNGROUPED_COLUMN_ID)
        assertTrue(ungrouped.cards.isEmpty(), "Tagged object must not also appear ungrouped")
    }

    @Test
    fun `viewGroups orders columns by index and drops hidden options`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val todoId = MockDataFactory.randomUuid()
        val doneId = MockDataFactory.randomUuid()
        val backlogId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        seedOptions(
            option(id = todoId, relationKey = groupRelationKey, name = "To Do"),
            option(id = doneId, relationKey = groupRelationKey, name = "Done"),
            option(id = backlogId, relationKey = groupRelationKey, name = "Backlog")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        // viewGroups intentionally passed in arbitrary input order to verify index sort.
        val viewGroups = listOf(
            ViewGroup(groupId = doneId, index = 1, hidden = false, backgroundColor = ""),
            ViewGroup(groupId = backlogId, index = 2, hidden = true, backgroundColor = ""),
            ViewGroup(groupId = todoId, index = 0, hidden = false, backgroundColor = "")
        )

        val result = dvViewer.buildKanbanView(
            objectIds = emptyList(),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = DefaultObjectStore(),
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder,
            viewGroups = viewGroups
        )
        advanceUntilIdle()

        // Backlog (hidden) is dropped; To Do (idx 0) precedes Done (idx 1); ungrouped tail.
        val orderedGroupIds = result.columns.map { it.groupId }
        assertEquals(listOf(todoId, doneId, KANBAN_UNGROUPED_COLUMN_ID), orderedGroupIds)
    }

    @Test
    fun `viewGroups can hide the ungrouped column via the empty-string groupId`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val todoId = MockDataFactory.randomUuid()
        val ungroupedObjId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                // Object with NO status — would normally end up in the ungrouped column.
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to ungroupedObjId,
                    Relations.NAME to "Has no status"
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        seedOptions(
            option(id = todoId, relationKey = groupRelationKey, name = "To Do")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val viewGroups = listOf(
            ViewGroup(groupId = todoId, index = 0, hidden = false, backgroundColor = ""),
            ViewGroup(groupId = KANBAN_UNGROUPED_COLUMN_ID, index = 1, hidden = true, backgroundColor = "")
        )

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(ungroupedObjId),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder,
            viewGroups = viewGroups
        )
        advanceUntilIdle()

        // Only the To Do column survives; the ungrouped column is suppressed entirely.
        assertEquals(listOf(todoId), result.columns.map { it.groupId })
    }

    @Test
    fun `options not referenced in viewGroups stay visible at the end`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val knownId = MockDataFactory.randomUuid()
        val newId = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        seedOptions(
            option(id = knownId, relationKey = groupRelationKey, name = "Known"),
            option(id = newId, relationKey = groupRelationKey, name = "Newly added elsewhere")
        )

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        // viewGroups only references the "known" option — the newly created option must
        // still appear so that users do not silently lose access to it.
        val viewGroups = listOf(
            ViewGroup(groupId = knownId, index = 0, hidden = false, backgroundColor = "")
        )

        val result = dvViewer.buildKanbanView(
            objectIds = emptyList(),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = DefaultObjectStore(),
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder,
            viewGroups = viewGroups
        )
        advanceUntilIdle()

        val groupIds = result.columns.map { it.groupId }
        assertTrue(newId in groupIds, "Newly added option should still render")
        assertTrue(groupIds.indexOf(knownId) < groupIds.indexOf(newId),
            "Known option (indexed) must precede the unindexed new option")
    }

    @Test
    fun `object whose group value references deleted option falls into ungrouped`() = runTest {
        val groupRelationKey = MockDataFactory.randomString()
        val deletedOptionId = MockDataFactory.randomUuid()
        val obj1Id = MockDataFactory.randomUuid()
        val viewerId = MockDataFactory.randomUuid()

        val dvViewer = Block.Content.DataView.Viewer(
            id = viewerId,
            name = "Board",
            type = Block.Content.DataView.Viewer.Type.BOARD,
            sorts = emptyList(),
            filters = emptyList(),
            viewerRelations = emptyList(),
            groupRelationKey = groupRelationKey
        )

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Orphan",
                    // Object points to an option that has been deleted from the relation.
                    groupRelationKey to listOf(deletedOptionId)
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )
        // Note: NO option seeded with id == deletedOptionId, simulating it being removed.

        val groupRelation = ObjectWrapper.Relation(mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to groupRelationKey,
            Relations.NAME to "Status",
            Relations.RELATION_FORMAT to Relation.Format.STATUS.code.toDouble()
        ))

        val result = dvViewer.buildKanbanView(
            objectIds = listOf(obj1Id),
            dataViewRelations = emptyList(),
            objectOrders = emptyList(),
            objectOrderIds = emptyList(),
            groupRelation = groupRelation,
            store = store,
            storeOfRelationOptions = storeOfRelationOptions,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            ungroupedColumnName = ungroupedColumnName,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        // No grouped columns (no options seeded) + ungrouped containing the orphan card.
        assertEquals(1, result.columns.size)
        val ungrouped = result.columns.single()
        assertEquals(KANBAN_UNGROUPED_COLUMN_ID, ungrouped.groupId)
        assertEquals(1, ungrouped.cards.size)
        assertEquals(obj1Id, ungrouped.cards.first().objectId)
    }
}
