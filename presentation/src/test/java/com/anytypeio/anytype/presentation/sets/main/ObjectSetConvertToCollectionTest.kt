package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.testIn
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetConvertToCollectionTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should start collection subscription after changing from set to collection`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            dvFilters = mockObjectSet.filters,
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
        )

        val stateFlow = stateReducer.state.testIn(backgroundScope)

        // TESTING

        viewModel.onStart(ctx = root)

        val firstState = stateFlow.awaitItem()
        assertIs<ObjectState.Init>(firstState)

        val secondState = stateFlow.awaitItem()
        assertIs<ObjectState.DataView.Set>(secondState)

        viewModel.onClickListener(clicked = ListenerType.Relation.TurnIntoCollection)

        val eventSetIsCollection = Event.Command.DataView.SetIsCollection(
            context = root,
            dv = mockObjectSet.dataView.id,
            isCollection = true
        )

        dispatcher.send(
            Payload(
                context = root,
                events = listOf(eventSetIsCollection)
            )
        )

        val thirdState = stateFlow.awaitItem()
        assertIs<ObjectState.DataView.Collection>(thirdState)

        advanceUntilIdle()

        verifyBlocking(repo, times(1)) {
            searchObjectsWithSubscription(
                eq(mockObjectSet.subscriptionId),
                eq(listOf()),
                eq(
                    mockObjectSet.filters + ObjectSearchConstants.defaultDataViewFilters(
                        mockObjectSet.workspaceId
                    )
                ),
                eq(ObjectSearchConstants.defaultDataViewKeys + mockObjectSet.dvKeys),
                eq(listOf()),
                eq(0L),
                eq(ObjectSetConfig.DEFAULT_LIMIT),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(mockObjectSet.root)
            )
        }
    }
}