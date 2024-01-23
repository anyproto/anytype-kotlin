package com.anytypeio.anytype.presentation.objects

import android.util.Log
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.PermittedConditions
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class CreateDVObjectTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel

    val root = MockDataFactory.randomString()
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
    val title =
        StubTitle(id = "title-${RandomString.make()}", text = "title-name-${RandomString.make()}")
    val header = StubHeader(id = "header-${RandomString.make()}", children = listOf(title.id))
    val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @Before
    fun setup() = runTest {
        repo = mock(verboseLogging = true)
        viewModel = givenViewModel()
        stubNetworkMode()
        storeOfRelations.merge(listOf(filterDate))
    }

    @After
    fun after() {
        rule.advanceTime()
    }

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

        stubOpenObject(
            doc = listOf(header, title, dataView),
            details = mockObjectSet.details
        )

    }

}