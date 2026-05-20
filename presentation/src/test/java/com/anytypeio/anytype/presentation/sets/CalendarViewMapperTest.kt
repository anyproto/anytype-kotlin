package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.misc.UrlBuilderImpl
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewMapperTest {

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
    private val urlBuilder by lazy { UrlBuilderImpl(gateway) }
    private val untitledPlaceholder = "Untitled"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fieldParser = FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        doReturn(untitledPlaceholder).whenever(stringResourceProvider).getUntitledObjectTitle()
    }

    private fun buildViewer(
        dateRelationKey: String,
        viewerRelations: List<Block.Content.DataView.Viewer.ViewerRelation> = emptyList()
    ) = Block.Content.DataView.Viewer(
        id = MockDataFactory.randomUuid(),
        name = "Calendar",
        type = Block.Content.DataView.Viewer.Type.CALENDAR,
        sorts = emptyList(),
        filters = emptyList(),
        viewerRelations = viewerRelations,
        groupRelationKey = dateRelationKey
    )

    private fun buildDateRelation(key: String) = ObjectWrapper.Relation(
        mapOf(
            Relations.ID to MockDataFactory.randomUuid(),
            Relations.RELATION_KEY to key,
            Relations.NAME to "Due Date",
            Relations.RELATION_FORMAT to Relation.Format.DATE.code.toDouble()
        )
    )

    @Test
    fun `objects with date values are assembled as entries with correct data`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val obj1Id = MockDataFactory.randomUuid()
        val obj2Id = MockDataFactory.randomUuid()
        val timestamp1 = 1716076800L // 2024-05-19 00:00 UTC
        val timestamp2 = 1718755200L // 2024-06-19 00:00 UTC

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj1Id,
                    Relations.NAME to "Task A",
                    dateKey to timestamp1.toDouble()
                )),
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to obj2Id,
                    Relations.NAME to "Task B",
                    dateKey to timestamp2.toDouble()
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        val result = viewer.buildCalendarView(
            objectIds = listOf(obj1Id, obj2Id),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(2, result.entries.size)
        val entry1 = result.entries.first { it.objectId == obj1Id }
        assertEquals("Task A", entry1.name)
        assertEquals(timestamp1, entry1.dateInSeconds)
        val entry2 = result.entries.first { it.objectId == obj2Id }
        assertEquals("Task B", entry2.name)
        assertEquals(timestamp2, entry2.dateInSeconds)
    }

    @Test
    fun `objects with null date value are excluded from entries`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val datedId = MockDataFactory.randomUuid()
        val undatedId = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to datedId,
                    Relations.NAME to "Has Date",
                    dateKey to timestamp.toDouble()
                )),
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to undatedId,
                    Relations.NAME to "No Date"
                    // dateKey absent → null
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        val result = viewer.buildCalendarView(
            objectIds = listOf(datedId, undatedId),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(1, result.entries.size)
        assertEquals(datedId, result.entries.single().objectId)
    }

    @Test
    fun `objects with zero timestamp (unset DATE) are excluded from entries`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val datedId = MockDataFactory.randomUuid()
        val zeroDateId = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to datedId,
                    Relations.NAME to "Has Date",
                    dateKey to timestamp.toDouble()
                )),
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to zeroDateId,
                    Relations.NAME to "Zero Date",
                    dateKey to 0.0 // protobuf default for unset DATE
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        val result = viewer.buildCalendarView(
            objectIds = listOf(datedId, zeroDateId),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(1, result.entries.size)
        assertEquals(datedId, result.entries.single().objectId)
    }

    @Test
    fun `hideName propagation - when NAME is invisible hideName is true on entries`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val objId = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to objId,
                    Relations.NAME to "My Task",
                    dateKey to timestamp.toDouble()
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewerRelations = listOf(
            Block.Content.DataView.Viewer.ViewerRelation(
                key = Relations.NAME,
                isVisible = false
            )
        )
        val viewer = buildViewer(dateRelationKey = dateKey, viewerRelations = viewerRelations)
        val dateRelation = buildDateRelation(key = dateKey)

        val result = viewer.buildCalendarView(
            objectIds = listOf(objId),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertTrue(result.entries.single().hideName)
    }

    @Test
    fun `invalid objects in store (no ID) are excluded from entries`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val validId = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to validId,
                    Relations.NAME to "Valid",
                    dateKey to timestamp.toDouble()
                )),
                // Invalid object — no ID. Stored under empty-string key in store
                // because ObjectWrapper.Basic.id defaults to "" when Relations.ID is missing.
                ObjectWrapper.Basic(mapOf(
                    Relations.NAME to "Invalid",
                    dateKey to timestamp.toDouble()
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        // Include "" in objectIds so the mapper actually iterates the invalid object
        // (store.get("") returns it; `!obj.isValid` short-circuits the loop).
        val result = viewer.buildCalendarView(
            objectIds = listOf(validId, ""),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(1, result.entries.size)
        assertEquals(validId, result.entries.single().objectId)
    }

    @Test
    fun `entry order follows objectOrderIds when provided`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(Relations.ID to id1, dateKey to timestamp.toDouble())),
                ObjectWrapper.Basic(mapOf(Relations.ID to id2, dateKey to timestamp.toDouble())),
                ObjectWrapper.Basic(mapOf(Relations.ID to id3, dateKey to timestamp.toDouble()))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        // Explicitly request id3, id1, id2 order
        val result = viewer.buildCalendarView(
            objectIds = listOf(id1, id2, id3),
            objectOrderIds = listOf(id3, id1, id2),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(3, result.entries.size)
        assertEquals(id3, result.entries[0].objectId)
        assertEquals(id1, result.entries[1].objectId)
        assertEquals(id2, result.entries[2].objectId)
    }

    @Test
    fun `duplicate objectIds produce only one entry`() = runTest {
        val dateKey = MockDataFactory.randomString()
        val objId = MockDataFactory.randomUuid()
        val timestamp = 1716076800L

        val store = DefaultObjectStore()
        store.merge(
            objects = listOf(
                ObjectWrapper.Basic(mapOf(
                    Relations.ID to objId,
                    Relations.NAME to "Task",
                    dateKey to timestamp.toDouble()
                ))
            ),
            dependencies = emptyList(),
            subscriptions = emptyList()
        )

        val viewer = buildViewer(dateRelationKey = dateKey)
        val dateRelation = buildDateRelation(key = dateKey)

        val result = viewer.buildCalendarView(
            objectIds = listOf(objId, objId, objId),
            objectOrderIds = emptyList(),
            dateRelation = dateRelation,
            store = store,
            storeOfObjectTypes = storeOfObjectTypes,
            fieldParser = fieldParser,
            urlBuilder = urlBuilder,
            untitledPlaceholder = untitledPlaceholder
        )
        advanceUntilIdle()

        assertEquals(1, result.entries.size)
    }
}
