package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.CellView
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
class ObjectSetNavigationTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `should emit command for editing relation-tag cell`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubStoreOfRelations(mockObjectSet)
        
        // Create dataView with CREATED_DATE relation link
        val relationLinksWithCreatedDate = mockObjectSet.relationLinks + StubRelationLink(Relations.CREATED_DATE, RelationFormat.DATE)
        val dataViewWithCreatedDate = StubDataView(
            id = mockObjectSet.dataView.id,
            views = listOf(mockObjectSet.viewer),
            relationLinks = relationLinksWithCreatedDate
        )
        
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, dataViewWithCreatedDate),
            details = mockObjectSet.details
        )
        val keys = mockObjectSet.dvKeys
        
        // Stub both subscription calls - one without CREATED_DATE and one with it
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = keys, // without createdDate
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

            // CLICK ON TAG RELATION
            viewModel.onGridCellClicked(
                cell = CellView.Tag(
                    id = mockObjectSet.obj1.id,
                    relationKey = mockObjectSet.relationObject3.key,
                    tags = listOf(),
                )
            )

            viewModel.commands.test {
                val command = awaitItem()
                assertIs<ObjectSetCommand.Modal.EditTagOrStatusCell>(command)
            }
        }
    }

    @Test
    fun `should close current object before navigating to some other object`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubStoreOfRelations(mockObjectSet)
        
        // Create dataView with CREATED_DATE relation link
        val relationLinksWithCreatedDate = mockObjectSet.relationLinks + StubRelationLink(Relations.CREATED_DATE, RelationFormat.DATE)
        val dataViewWithCreatedDate = StubDataView(
            id = mockObjectSet.dataView.id,
            views = listOf(mockObjectSet.viewer),
            relationLinks = relationLinksWithCreatedDate
        )
        
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, dataViewWithCreatedDate),
            details = mockObjectSet.details
        )

        // Stub both subscription calls - one without CREATED_DATE and one with it
        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            keys = mockObjectSet.dvKeys, // without createdDate
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        stubCloseBlock()

        // TESTING
        proceedWithStartingViewModel()

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            // CLICK ON RECORD NAME
            viewModel.onObjectHeaderClicked(mockObjectSet.obj1.id)

            // CHECK CLOSE BLOCK COMMAND
            advanceUntilIdle()
            verifyBlocking(closeObject, times(1)) {
                async(
                    CloseObject.Params(
                        mockObjectSet.root,
                        SpaceId(defaultSpace)
                    )
                )
            }
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}