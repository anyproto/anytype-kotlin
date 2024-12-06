package com.anytypeio.anytype.presentation.collections

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
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

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionDataViewUpdateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var objectCollection: MockCollection

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        objectCollection = MockCollection(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should restart subscription when sort is updated`() = runTest {
        // SETUP
        stubSpaceManager(objectCollection.spaceId)
        stubStoreOfRelations(objectCollection)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                objectCollection.header,
                objectCollection.title,
                objectCollection.dataView
            ),
            details = objectCollection.details
        )

        stubSubscriptionResults(
            subscription = objectCollection.subscriptionId,
            collection = root,
            spaceId = objectCollection.spaceId,
            storeOfRelations = storeOfRelations,
            keys = objectCollection.dvKeys,
            objects = listOf(objectCollection.obj1, objectCollection.obj2),
            dvSorts = objectCollection.sorts
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT COLLECTION OBJECT STATE
        stateReducer.state.test {
            val first = awaitItem()
            assertIs<ObjectState.Init>(first)

            val second = awaitItem()
            assertIs<ObjectState.DataView.Collection>(second)

            //after payload
            val eventDVUpdate = Event.Command.DataView.UpdateView(
                context = objectCollection.root,
                block = objectCollection.dataView.id,
                viewerId = objectCollection.viewerList.id,
                sortUpdates = listOf(
                    Event.Command.DataView.UpdateView.DVSortUpdate.Update(
                        id = objectCollection.sort1.id,
                        sort = DVSort(
                            id = objectCollection.sort1.id,
                            relationKey = objectCollection.sort1.relationKey,
                            type = DVSortType.DESC,
                            relationFormat = objectCollection.sort1.relationFormat
                        )
                    )
                )
            )

            val newSort = objectCollection.sort1.copy(
                type = Block.Content.DataView.Sort.Type.DESC
            )

            stubSubscriptionResults(
                subscription = objectCollection.subscriptionId,
                collection = root,
                spaceId = objectCollection.spaceId,
                storeOfRelations = storeOfRelations,
                keys = objectCollection.dvKeys,
                objects = listOf(objectCollection.obj1, objectCollection.obj2),
                dvSorts = listOf(newSort)
            )

            advanceUntilIdle()
            dispatcher.send(
                Payload(
                    context = root,
                    events = listOf(eventDVUpdate)
                )
            )

            val third = awaitItem()
            assertIs<ObjectState.DataView.Collection>(third)
            assertEquals(
                expected = listOf(newSort),
                actual = third.dataViewContent.viewers.first().sorts
            )
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}