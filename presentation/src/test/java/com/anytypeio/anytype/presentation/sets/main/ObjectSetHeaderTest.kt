package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.SetOrCollectionHeaderState
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ObjectSetHeaderTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, space = defaultSpace)
        stubNetworkMode()
        stubObservePermissions()
        stubAnalyticSpaceHelperDelegate()
    }

    @After
    fun after() {
        rule.advanceTime(100)
    }

    @Test
    fun `should return header with title but without emoji`() = runTest {

        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )
        storeOfRelations.merge(
            listOf(
                mockObjectSet.relationObject1,
                mockObjectSet.relationObject2,
                mockObjectSet.relationObject3,
                mockObjectSet.relationObject4
            )
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertEquals(
                expected = mockObjectSet.title.id,
                actual = (viewModel.header.value as SetOrCollectionHeaderState.Default).title.id
            )

            assertEquals(
                expected = null,
                actual = mockObjectSet.header.fields.iconEmoji
            )
        }
    }

    @Test
    fun `should return header with title but with emoji`() = runTest {

        stubSpaceManager(mockObjectSet.spaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(
                mockObjectSet.headerWithEmoji,
                mockObjectSet.title,
                mockObjectSet.dataView
            ),
            details = mockObjectSet.details
        )
        storeOfRelations.merge(
            listOf(
                mockObjectSet.relationObject1,
                mockObjectSet.relationObject2,
                mockObjectSet.relationObject3,
                mockObjectSet.relationObject4
            )
        )
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            assertEquals(
                expected = mockObjectSet.title.id,
                actual = (viewModel.header.value as SetOrCollectionHeaderState.Default).title.id
            )

            assertEquals(
                expected = mockObjectSet.emoji,
                actual = mockObjectSet.headerWithEmoji.fields.iconEmoji
            )
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}