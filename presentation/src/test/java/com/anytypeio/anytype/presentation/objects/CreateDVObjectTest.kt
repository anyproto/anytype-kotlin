package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.PermittedConditions
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.ext.DAYS_IN_MONTH
import com.anytypeio.anytype.core_models.ext.DAYS_IN_WEEK
import com.anytypeio.anytype.core_models.ext.SECONDS_IN_DAY
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.prefillNewObjectDetails
import com.anytypeio.anytype.presentation.sets.resolveSetByRelationPrefilledObjectData
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@OptIn(ExperimentalCoroutinesApi::class)
class CreateDVObjectTest : ObjectSetViewModelTestSetup() {

    @Mock
    lateinit var dateProvider: DateProvider

    private val timestamp = 1703775402L
    private val spaceId = "spaceId-${MockDataFactory.randomString()}"
    private val filterDate = StubRelationObject(
        id = "dueDateId-${MockDataFactory.randomString()}",
        key = "dueDateKey-${MockDataFactory.randomString()}",
        format = RelationFormat.DATE,
        isReadOnlyValue = false,
        spaceId = spaceId
    )

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() = runTest {
        dateProvider = mock(verboseLogging = true)
        storeOfRelations.merge(listOf(filterDate))
        dateProvider.stub {
            on { getCurrentTimestampInSeconds() } doReturn timestamp
        }
    }

    @Test
    fun `should create object with prefilled details from filters in set by relation`() = runTest {

        val prefilledRelation = StubRelationObject(
            id = "prefilledRelationId-${MockDataFactory.randomString()}",
            key = "prefilledRelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.OBJECT
        )
        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter2Relation = StubRelationObject(
            id = "filter2RelationId-${MockDataFactory.randomString()}",
            key = "filter2RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.CHECKBOX,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter3Relation = StubRelationObject(
            id = "filter3RelationId-${MockDataFactory.randomString()}",
            key = "filter3RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = true,
            spaceId = spaceId
        )
        val filter4Relation = StubRelationObject(
            id = "filter4RelationId-${MockDataFactory.randomString()}",
            key = "filter4RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter2Relation.key, RelationFormat.CHECKBOX),
            RelationLink(filter3Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter4Relation.key, RelationFormat.SHORT_TEXT)
        )

        storeOfRelations.merge(
            listOf(
                prefilledRelation,
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in PermittedConditions }

        val filters = listOf(
            DVFilter(
                relation = filter1Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.EQUAL,
                value = "321"
            ),
            DVFilter(
                relation = filter2Relation.key,
                relationFormat = RelationFormat.CHECKBOX,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.EQUAL,
                value = true
            ),
            DVFilter(
                relation = filter3Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = DVFilterOperator.AND,
                condition = DVFilterCondition.EQUAL,
                value = "456"
            ),
            DVFilter(
                relation = filter4Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = DVFilterOperator.AND,
                condition = notPermittedConditions.random(),
                value = "456"
            )
        )

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filter1Relation.key),
                    StubDataViewViewRelation(key = filter2Relation.key),
                    StubDataViewViewRelation(key = filter3Relation.key),
                    StubDataViewViewRelation(key = filter4Relation.key)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val struct = dvViewer.resolveSetByRelationPrefilledObjectData(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            objSetByRelation = prefilledRelation,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedStruct = mapOf(
            prefilledRelation.key to null,
            filter1Relation.key to "321",
            filter2Relation.key to true
        )

        assertEquals(expectedStruct, struct)
    }

    @Test
    fun `should update value prefilled setof relation by filter value`() = runTest {

        val prefilledRelation = StubRelationObject(
            id = "prefilledRelationId-${MockDataFactory.randomString()}",
            key = "prefilledRelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT
        )
        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.SHORT_TEXT)
        )

        storeOfRelations.merge(
            listOf(
                prefilledRelation,
                filter1Relation
            )
        )

        val filters = listOf(
            DVFilter(
                relation = prefilledRelation.key,
                relationFormat = prefilledRelation.relationFormat,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "456"
            )
        )

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filter1Relation.key)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val struct = dvViewer.resolveSetByRelationPrefilledObjectData(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            objSetByRelation = prefilledRelation,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedStruct = mapOf(
            prefilledRelation.key to "456"
        )

        assertEquals(expectedStruct, struct)
    }

    @Test
    fun `should create object with prefilled details from filters in set by type`() = runTest {

        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter2Relation = StubRelationObject(
            id = "filter2RelationId-${MockDataFactory.randomString()}",
            key = "filter2RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.CHECKBOX,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter3Relation = StubRelationObject(
            id = "filter3RelationId-${MockDataFactory.randomString()}",
            key = "filter3RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = true,
            spaceId = spaceId
        )
        val filter4Relation = StubRelationObject(
            id = "filter4RelationId-${MockDataFactory.randomString()}",
            key = "filter4RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter2Relation.key, RelationFormat.CHECKBOX),
            RelationLink(filter3Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter4Relation.key, RelationFormat.SHORT_TEXT)
        )

        storeOfRelations.merge(
            listOf(
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in PermittedConditions }

        val filters = listOf(
            DVFilter(
                relation = filter1Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "321"
            ),
            DVFilter(
                relation = filter2Relation.key,
                relationFormat = RelationFormat.CHECKBOX,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = true
            ),
            DVFilter(
                relation = filter3Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "456"
            ),
            DVFilter(
                relation = filter4Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = notPermittedConditions.random(),
                value = "456"
            )
        )

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filter1Relation.key),
                    StubDataViewViewRelation(key = filter2Relation.key),
                    StubDataViewViewRelation(key = filter3Relation.key),
                    StubDataViewViewRelation(key = filter4Relation.key)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val struct = dvViewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedStruct = mapOf(
            filter1Relation.key to "321",
            filter2Relation.key to true
        )

        assertEquals(expectedStruct, struct)
    }

    @Test
    fun `should create object with prefilled details from filters in collection`() = runTest {

        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter2Relation = StubRelationObject(
            id = "filter2RelationId-${MockDataFactory.randomString()}",
            key = "filter2RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.CHECKBOX,
            isReadOnlyValue = false,
            spaceId = spaceId
        )
        val filter3Relation = StubRelationObject(
            id = "filter3RelationId-${MockDataFactory.randomString()}",
            key = "filter3RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = true,
            spaceId = spaceId
        )
        val filter4Relation = StubRelationObject(
            id = "filter4RelationId-${MockDataFactory.randomString()}",
            key = "filter4RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter2Relation.key, RelationFormat.CHECKBOX),
            RelationLink(filter3Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter4Relation.key, RelationFormat.SHORT_TEXT)
        )

        storeOfRelations.merge(
            listOf(
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in PermittedConditions }

        val filters = listOf(
            DVFilter(
                relation = filter1Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "321"
            ),
            DVFilter(
                relation = filter2Relation.key,
                relationFormat = RelationFormat.CHECKBOX,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = true
            ),
            DVFilter(
                relation = filter3Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "456"
            ),
            DVFilter(
                relation = filter4Relation.key,
                relationFormat = RelationFormat.SHORT_TEXT,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = notPermittedConditions.random(),
                value = "456"
            )
        )

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filter1Relation.key),
                    StubDataViewViewRelation(key = filter2Relation.key),
                    StubDataViewViewRelation(key = filter3Relation.key),
                    StubDataViewViewRelation(key = filter4Relation.key)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val struct = dvViewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedStruct = mapOf(
            filter1Relation.key to "321",
            filter2Relation.key to true
        )

        assertEquals(expectedStruct, struct)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createObjectWithQuickOption(
        quickOption: DVFilterQuickOption,
        filterValue: Long? = null,
        expectedValue: Long?
    ) = runTest {
        val filters = listOf(
            DVFilter(
                relation = filterDate.key,
                relationFormat = RelationFormat.DATE,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                quickOption = quickOption,
                value = filterValue
            )
        )

        val dvRelationLinks = listOf(
            RelationLink(filterDate.key, RelationFormat.DATE)
        )

        storeOfRelations.merge(listOf(filterDate))

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filterDate.key, isVisible = true)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val prefilled = dvViewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedPrefilled = mapOf(
            filterDate.key to (expectedValue?.toDouble() ?: timestamp.toDouble()),
        )
        assertEquals(expectedPrefilled, prefilled)
    }

    @Test
    fun `should create object with proper today timestamp on quick option Today`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.TODAY,
            expectedValue = timestamp
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Days Ago`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.DAYS_AGO,
            filterValue = 99L,
            expectedValue = timestamp - SECONDS_IN_DAY * 99
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Last Month`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.LAST_MONTH,
            expectedValue = timestamp - SECONDS_IN_DAY * DAYS_IN_MONTH
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Last Week`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.LAST_WEEK,
            expectedValue = timestamp - SECONDS_IN_DAY * DAYS_IN_WEEK
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Yesterday`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.YESTERDAY,
            expectedValue = timestamp - SECONDS_IN_DAY
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Current Week`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.CURRENT_WEEK,
            expectedValue = timestamp
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Current Month`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.CURRENT_MONTH,
            expectedValue = timestamp
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Next Week`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.NEXT_WEEK,
            expectedValue = timestamp + SECONDS_IN_DAY * DAYS_IN_WEEK
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Next Month`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.NEXT_MONTH,
            expectedValue = timestamp + SECONDS_IN_DAY * DAYS_IN_MONTH
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Tomorrow`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.TOMORROW,
            expectedValue = timestamp + SECONDS_IN_DAY
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Days Ahead`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.DAYS_AHEAD,
            filterValue = 99L,
            expectedValue = timestamp + SECONDS_IN_DAY * 99
        )
    }

    @Test
    fun `should create object with proper today timestamp on quick option Exact Day`() {
        createObjectWithQuickOption(
            quickOption = DVFilterQuickOption.EXACT_DATE,
            filterValue = timestamp,
            expectedValue = timestamp
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should take relation format from relation link`() = runTest {

        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.DATE,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        storeOfRelations.merge(listOf(filter1Relation))

        val filters = listOf(
            DVFilter(
                relation = filter1Relation.key,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = true
            )
        )

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.CHECKBOX),
        )

        val dvViewer =
            StubDataViewView(
                id = "dvViewerList-${RandomString.make()}",
                viewerRelations = listOf(
                    StubDataViewViewRelation(key = filter1Relation.key)
                ),
                type = DVViewerType.LIST,
                filters = filters
            )

        val struct = dvViewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = dvRelationLinks,
        )

        val expectedStruct = mapOf(filter1Relation.key to true)

        assertEquals(expectedStruct, struct)
    }
}