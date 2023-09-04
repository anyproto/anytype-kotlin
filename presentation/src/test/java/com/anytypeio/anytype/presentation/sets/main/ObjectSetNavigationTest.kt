package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetCommand
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetNavigationTest : ObjectSetViewModelTestSetup() {

    private lateinit var closable: AutoCloseable
    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var mockObjectSet: MockSet

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
        mockObjectSet = MockSet(context = root)
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    @Test
    fun `should emit command for editing relation-tag cell`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
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
            assertIs<DataViewViewState.Set.Default>(second)

            // CLICK ON TAG RELATION
            viewModel.onGridCellClicked(
                cell = CellView.Tag(
                    id = mockObjectSet.obj1.id,
                    relationKey = mockObjectSet.relationObject3.key,
                    tags = listOf()
                )
            )

            viewModel.commands.test {
                val command = awaitItem()
                assertIs<ObjectSetCommand.Modal.EditRelationCell>(command)
            }
        }
    }

    @Test
    fun `should emit command for editing relation-object cell if this relation is read-only and object's layout is supported`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )

        val targetObjectId = MockDataFactory.randomUuid()
        val object3 = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                mockObjectSet.relationObject4.key to targetObjectId
            )
        )
        val targetObject = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to targetObjectId,
                Relations.LAYOUT to SupportedLayouts.layouts.random().code.toDouble()
            )
        )

        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2, object3),
            dependencies = listOf(targetObject),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            // CLICK ON TAG RELATION
            viewModel.onGridCellClicked(
                cell = CellView.Object(
                    id = object3.id,
                    relationKey = mockObjectSet.relationObject4.key,
                    objects = listOf(
                        ObjectView.Default(
                            id = targetObjectId,
                            name = "",
                            icon = ObjectIcon.None
                        )
                    )
                )
            )

            viewModel.commands.test {
                val command = awaitItem()
                assertIs<ObjectSetCommand.Modal.EditRelationCell>(command)
            }
        }
    }

    @Test
    fun `should close current object before navigating to some other object`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
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
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2),
            dvFilters = mockObjectSet.filters
        )

        doReturn(Unit).`when`(closeBlock).async(mockObjectSet.root)

        // TESTING
        viewModel.onStart(ctx = root)

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
            verifyBlocking(closeBlock, times(1)) {
                async(mockObjectSet.root)
            }
        }
    }

    @Test
    fun `should not emit any navigation command for opening an object if object's layout is not supported`() = runTest {
        // SETUP
        stubWorkspaceManager(mockObjectSet.workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )

        val unsupportedLayouts =
            ObjectType.Layout.values().toList() - SupportedLayouts.layouts.toSet()
        val unsupportedLayout = unsupportedLayouts.random()
        val objectUnsupportedLayout = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to "objectUnsupportedLayout-${RandomString.make()}",
                Relations.LAYOUT to unsupportedLayout.code.toDouble()
            )
        )

        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            workspace = mockObjectSet.workspaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2, objectUnsupportedLayout),
            dependencies = listOf(),
            dvFilters = mockObjectSet.filters
        )

        // TESTING
        viewModel.onStart(ctx = root)

        // ASSERT DATA VIEW STATE
        viewModel.currentViewer.test {
            val first = awaitItem()
            assertIs<DataViewViewState.Init>(first)

            val second = awaitItem()
            assertIs<DataViewViewState.Set.Default>(second)

            // CLICK ON RECORD NAME
            viewModel.onObjectHeaderClicked(objectUnsupportedLayout.id)

            // CHECK ERROR TOAST
            viewModel.toasts.test {
                val item = awaitItem()
                assertEquals("Unexpected layout: $unsupportedLayout", item)
            }
            // CHECK CLOSE BLOCK COMMAND NOT EMITTED
            advanceUntilIdle()
            verifyNoInteractions(closeBlock)
        }
    }
}