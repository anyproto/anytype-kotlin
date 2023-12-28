package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.config.ConfigStorage
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class CreateDataViewObjectUseCaseTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    lateinit var spaceManager: SpaceManager

    lateinit var dispatchers: AppCoroutineDispatchers
    lateinit var createDataViewObject: CreateDataViewObject

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        repo = mock(verboseLogging = true)
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
            dispatchers = dispatchers
        )
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
            format = RelationFormat.SHORT_TEXT
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

        val notPermittedConditions = DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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
            relations = listOf(prefilledRelation.id),
            filters = filters
        )

        createDataViewObject.async(params)

        val expected = Command.CreateObject(
            template = template,
            prefilled = mapOf(
                prefilledRelation.key to "",
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
            relations = listOf(prefilledRelation.id),
            filters = filters
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

        val notPermittedConditions = DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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

        val params = CreateDataViewObject.Params.SetByType(
            template = template,
            type = TypeKey(type),
            filters = filters
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

        storeOfRelations.merge(
            listOf(
                filter1Relation,
                filter2Relation,
                filter3Relation,
                filter4Relation
            )
        )
        spaceManager.set(spaceId)

        val notPermittedConditions = DVFilterCondition.values().filterNot { it in CreateDataViewObject.permittedConditions }

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
            filters = filters
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
}