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
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
    }

    @Test
    fun `should show error toast when clicking on read-only cell, and edit the cell when it's not read-only`() =
        runTest {
            // SETUP
            stubWorkspaceManager(mockObjectSet.workspaceId)
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
                workspace = mockObjectSet.workspaceId,
                storeOfRelations = storeOfRelations,
                keys = mockObjectSet.dvKeys,
                sources = listOf(mockObjectSet.setOf),
                objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
                dvFilters = mockObjectSet.filters
            )

            // TESTING
            viewModel.onStart(ctx = root)

            // ASSERT DATA VIEW STATE
            viewModel.currentViewer.test {
                val first = awaitItem()
                assertIs<DataViewViewState.Init>(first)

                val second = awaitItem()
                assertIs<DataViewViewState.Set.NoItems>(second)

                val third = awaitItem()
                assertIs<DataViewViewState.Set.Default>(third)

                // CLICK ON READ ONLY VALUE RELATION
                viewModel.onGridCellClicked(
                    cell = CellView.Description(
                        id = mockObjectSet.obj1.id,
                        relationKey = mockObjectSet.relationObject2.key,
                        text = ""
                    )
                )

                // CLICK ON EDITABLE VALUE RELATION
                viewModel.onGridCellClicked(
                    cell = CellView.Description(
                        id = mockObjectSet.obj1.id,
                        relationKey = mockObjectSet.relationObject1.key,
                        text = ""
                    )
                )

                viewModel.commands.test {
                    val command = awaitItem()
                    assertIs<ObjectSetCommand.Modal.EditGridTextCell>(command)
                }
            }
        }
}