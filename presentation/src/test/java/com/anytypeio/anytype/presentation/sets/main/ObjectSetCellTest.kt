package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.CellView
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetCellTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root, space = defaultSpace)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should edit the cell when it's not read-only and read-only`() =
        runTest {
            // SETUP
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

                // CLICK ON READ ONLY VALUE RELATION
                viewModel.onGridCellClicked(
                    cell = CellView.Description(
                        id = mockObjectSet.obj1.id,
                        relationKey = mockObjectSet.relationObject2.key,
                        text = "",
                        space = mockObjectSet.space
                    )
                )

                // CLICK ON EDITABLE VALUE RELATION
                viewModel.onGridCellClicked(
                    cell = CellView.Description(
                        id = mockObjectSet.obj1.id,
                        relationKey = mockObjectSet.relationObject1.key,
                        text = "",
                        space = mockObjectSet.space
                    )
                )

                viewModel.commands.test {
                    val command = awaitItem()
                    assertIs<ObjectSetCommand.Modal.EditGridTextCell>(command)
                    val command2 = awaitItem()
                    assertIs<ObjectSetCommand.Modal.EditGridTextCell>(command2)
                }
            }
        }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}