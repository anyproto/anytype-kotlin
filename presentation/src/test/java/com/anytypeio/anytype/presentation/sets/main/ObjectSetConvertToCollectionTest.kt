package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetConvertToCollectionTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should start collection subscription after changing from set to collection`() = runTest {
        turbineScope {

            // SETUP
            stubSpaceManager(defaultSpace)
            stubObjectToCollection()
            stubOpenObject(
                doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
                details = mockObjectSet.details
            )
            stubSubscriptionResults(
                subscription = mockObjectSet.subscriptionId,
                spaceId = mockObjectSet.spaceId,
                keys = mockObjectSet.dvKeys,
                sources = listOf(mockObjectSet.setOf),
                dvFilters = mockObjectSet.filters,
                objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2)
            )

            val stateFlow = stateReducer.state.testIn(backgroundScope)

            // TESTING

            // Making sure space ids are not mixed up:

            assertEquals(
                expected = defaultSpace,
                actual = mockObjectSet.spaceId
            )

            assertEquals(
                expected = mockObjectSet.space,
                actual = mockObjectSet.spaceId
            )

            assertEquals(
                expected = defaultSpace,
                actual = spaceManager.getConfig()?.space
            )

            proceedWithStartingViewModel()

            val firstState = stateFlow.awaitItem()
            assertIs<ObjectState.Init>(firstState)

            val secondState = stateFlow.awaitItem()
            assertIs<ObjectState.DataView.Set>(secondState)

            viewModel.proceedWithConvertingToCollection()

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

            // Verify that the collection subscription was called (may be among multiple calls)
            verifyBlocking(repo, times(1)) {
                searchObjectsWithSubscription(
                    SpaceId(mockObjectSet.space),
                    mockObjectSet.subscriptionId,
                    listOf(
                        Block.Content.DataView.Sort(
                            relationKey = Relations.CREATED_DATE,
                            type = Block.Content.DataView.Sort.Type.DESC,
                            relationFormat = RelationFormat.DATE,
                            includeTime = true
                        )
                    ),
                    mockObjectSet.filters + ObjectSearchConstants.defaultDataViewFilters(),
                    ObjectSearchConstants.defaultDataViewKeys + mockObjectSet.dvKeys, // collections should not include CREATED_DATE
                    listOf(), // collections should have empty sources
                    0L,
                    ObjectSetConfig.DEFAULT_LIMIT,
                    null,
                    null,
                    null,
                    null,
                    collection = mockObjectSet.root // collection parameter should be set
                )
            }
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}