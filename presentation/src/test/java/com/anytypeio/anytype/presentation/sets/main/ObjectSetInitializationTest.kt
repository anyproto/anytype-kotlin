package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

class ObjectSetInitializationTest : ObjectSetViewModelTestSetup() {

    private val title = StubTitle()
    private val header = StubHeader(children = listOf(title.id))

    private val ctx: Id = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        initDataViewSubscriptionContainer()
    }

    @Test
    fun `should not start creating new record if dv is not initialized yet`() {

        // SETUP

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            )
        )

        openObjectSet.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Left(
                Exception("Error while opening object set")
            )
        }

        val vm = givenViewModel()

        // TESTING

        vm.onStart(ctx = ctx)
        vm.onCreateNewDataViewObject()

       verifyNoInteractions(createObject)
    }

    @Test
    fun `when open object set, should start subscription to the records by object setOf details`() {

        stubInterceptEvents()
        stubInterceptThreadStatus(InterceptThreadStatus.Params(ctx))
        initDataViewSubscriptionContainer()
        stubSearchWithSubscription()
        stubSubscriptionEventChannel()

        val view = StubDataViewView()
        val relLink1 = StubRelationLink()
        val relLink2 = StubRelationLink()

        val dataView = StubDataView(
            id = MockDataFactory.randomUuid(),
            views = listOf(view),
            relations = listOf(relLink1, relLink2),
            targetObjectId = MockDataFactory.randomUuid()
        )

        val type = MockDataFactory.randomString()
        stubOpenObjectSet(
            doc = listOf(header, title, dataView),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf(Relations.SET_OF to listOf(type))
                    )
                )
            )
        )

        val vm = givenViewModel()

        vm.onStart(ctx = root)

        verifyBlocking(repo, times(1)) {
            searchObjectsWithSubscription(
                subscription = root,
                sorts = listOf(),
                filters = buildList { addAll(ObjectSearchConstants.defaultDataViewFilters()) },
                keys = ObjectSearchConstants.defaultDataViewKeys.distinct() + listOf(relLink1, relLink2).map { it.key },
                source = arrayListOf(type),
                offset = 0,
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                beforeId = null,
                afterId = null,
                ignoreWorkspace = null,
                noDepSubscription = null
            )
        }

        coroutineTestRule.advanceTime(100L)
    }
}