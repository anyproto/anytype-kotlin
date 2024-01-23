package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class CreateDataViewObjectUseCaseTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var dateProvider: DateProvider

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    lateinit var spaceManager: SpaceManager

    lateinit var dispatchers: AppCoroutineDispatchers
    lateinit var createDataViewObject: CreateDataViewObject

    private val SECONDS_IN_DAY = 86400L

    private val timestamp = 1703775402L
    private val spaceId = "spaceId-${MockDataFactory.randomString()}"
    private val type = "type-${MockDataFactory.randomString()}"
    private val template = "template-${MockDataFactory.randomString()}"
    private val filterDate = StubRelationObject(
        id = "dueDateId-${MockDataFactory.randomString()}",
        key = "dueDateKey-${MockDataFactory.randomString()}",
        format = RelationFormat.DATE,
        isReadOnlyValue = false,
        spaceId = spaceId
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() = runTest {
        repo = mock(verboseLogging = true)
        dateProvider = mock(verboseLogging = true)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        spaceManager = SpaceManager.Impl(
            repo = repo,
            dispatchers = dispatchers,
            configStorage = ConfigStorage.CacheStorage(),
            logger = mock()
        )
        createDataViewObject = CreateDataViewObject(
            repo = repo,
            storeOfRelations = storeOfRelations,
            spaceManager = spaceManager,
            dispatchers = dispatchers,
            dateProvider = dateProvider
        )
        storeOfRelations.merge(listOf(filterDate))
        spaceManager.set(spaceId)
        dateProvider.stub {
            on { getCurrentTimestampInSeconds() } doReturn timestamp
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should create object with prefilled details from filters in set by relation`() = runTest {

        val spaceId = "spaceId-${MockDataFactory.randomString()}"
        val type = "type-${MockDataFactory.randomString()}"
        val template = "template-${MockDataFactory.randomString()}"
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
        spaceManager.set(spaceId)

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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

        val params = CreateDataViewObject.Params.SetByRelation(
            template = template,
            type = TypeKey(type),
            objSetByRelation = prefilledRelation,
            filters = filters,
            dvRelationLinks = dvRelationLinks
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                prefilledRelation.key to null,
                filter1Relation.key to "321",
                filter2Relation.key to true
            ),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should update value prefilled setof relation by filter value`() = runTest {

        val spaceId = "spaceId-${MockDataFactory.randomString()}"
        val type = "type-${MockDataFactory.randomString()}"
        val template = "template-${MockDataFactory.randomString()}"
        val prefilledRelation = StubRelationObject(
            id = "prefilledRelationId-${MockDataFactory.randomString()}",
            key = "prefilledRelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.SHORT_TEXT
        )

        val dvRelationLinks = listOf(
            RelationLink(prefilledRelation.key, RelationFormat.SHORT_TEXT),
        )

        storeOfRelations.merge(
            listOf(
                prefilledRelation
            )
        )
        spaceManager.set(spaceId)

        val filters = listOf(
            DVFilter(
                relation = prefilledRelation.key,
                relationFormat = prefilledRelation.relationFormat,
                operator = Block.Content.DataView.Filter.Operator.AND,
                condition = Block.Content.DataView.Filter.Condition.EQUAL,
                value = "456"
            )
        )

        val params = CreateDataViewObject.Params.SetByRelation(
            template = template,
            type = TypeKey(type),
            filters = filters,
            dvRelationLinks = dvRelationLinks,
            objSetByRelation = prefilledRelation
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                prefilledRelation.key to "456"
            ),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should create object with prefilled details from filters in set by type`() = runTest {

        val spaceId = "spaceId-${MockDataFactory.randomString()}"
        val type = "type-${MockDataFactory.randomString()}"
        val template = "template-${MockDataFactory.randomString()}"
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

        storeOfRelations.merge(
            listOf(
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )
        spaceManager.set(spaceId)

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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

        val dvRelationLinks = listOf(
            RelationLink(filter1Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter2Relation.key, RelationFormat.CHECKBOX),
            RelationLink(filter3Relation.key, RelationFormat.SHORT_TEXT),
            RelationLink(filter4Relation.key, RelationFormat.SHORT_TEXT)
        )

        val params = CreateDataViewObject.Params.SetByType(
            template = template,
            type = TypeKey(type),
            filters = filters,
            dvRelationLinks = dvRelationLinks
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                filter1Relation.key to "321",
                filter2Relation.key to true
            ),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should create object with prefilled details from filters in collection`() = runTest {

        val spaceId = "spaceId-${MockDataFactory.randomString()}"
        val type = "type-${MockDataFactory.randomString()}"
        val template = "template-${MockDataFactory.randomString()}"
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
            RelationLink(filter1Relation.id, RelationFormat.SHORT_TEXT),
            RelationLink(filter2Relation.id, RelationFormat.CHECKBOX),
            RelationLink(filter3Relation.id, RelationFormat.SHORT_TEXT),
            RelationLink(filter4Relation.id, RelationFormat.SHORT_TEXT)
        )

        storeOfRelations.merge(
            listOf(
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )
        spaceManager.set(spaceId)

        val notPermittedConditions =
            DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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

        val params = CreateDataViewObject.Params.Collection(
            template = template,
            type = TypeKey(type),
            filters = filters,
            dvRelationLinks = dvRelationLinks
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                filter1Relation.key to "321",
                filter2Relation.key to true
            ),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
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

        val params = CreateDataViewObject.Params.SetByType(
            template = template,
            type = TypeKey(type),
            filters = filters,
            dvRelationLinks = dvRelationLinks
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                filterDate.key to (expectedValue?.toDouble() ?: timestamp.toDouble()),
            ),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
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

        val spaceId = "spaceId-${MockDataFactory.randomString()}"
        val type = "type-${MockDataFactory.randomString()}"
        val template = "template-${MockDataFactory.randomString()}"
        val filter1Relation = StubRelationObject(
            id = "filter1RelationId-${MockDataFactory.randomString()}",
            key = "filter1RelationKey-${MockDataFactory.randomString()}",
            format = RelationFormat.DATE,
            isReadOnlyValue = false,
            spaceId = spaceId
        )

        storeOfRelations.merge(listOf(filter1Relation))
        spaceManager.set(spaceId)

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

        val params = CreateDataViewObject.Params.SetByType(
            template = template,
            type = TypeKey(type),
            filters = filters,
            dvRelationLinks = dvRelationLinks
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(filter1Relation.key to true),
            internalFlags = listOf(InternalFlags.ShouldSelectTemplate),
            space = SpaceId(spaceId),
            typeKey = TypeKey(type)
        )

        verifyBlocking(repo, times(1)) {
            createObject(command = expected)
        }
    }
}