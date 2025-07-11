package com.anytypeio.anytype.presentation.sets.main

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.objects.ObjectIcon
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
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
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
    fun `should emit command for editing relation-object cell if this relation is read-only and object's layout is supported`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )

        val targetObjectId = MockDataFactory.randomUuid()
        val object3 = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.SPACE_ID to mockObjectSet.spaceId,
                mockObjectSet.relationObject4.key to targetObjectId
            )
        )
        val targetObject = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to targetObjectId,
                Relations.SPACE_ID to mockObjectSet.spaceId,
                Relations.LAYOUT to SupportedLayouts.layouts.random().code.toDouble()
            )
        )

        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2, object3),
            dependencies = listOf(targetObject),
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
                cell = CellView.Object(
                    id = object3.id,
                    relationKey = mockObjectSet.relationObject4.key,
                    objects = listOf(
                        ObjectView.Default(
                            id = targetObjectId,
                            name = "",
                            icon = ObjectIcon.None,
                        )
                    ),
                )
            )

            viewModel.commands.test {
                val command = awaitItem()
                assertIs<ObjectSetCommand.Modal.EditObjectCell>(command)
            }
        }
    }

    @Test
    fun `should close current object before navigating to some other object`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
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

    @Test
    fun `should not emit any navigation command for opening an object if object's layout is not supported`() = runTest {
        // SETUP
        stubSpaceManager(mockObjectSet.spaceId)
        stubStoreOfRelations(mockObjectSet)
        stubOpenObject(
            doc = listOf(mockObjectSet.header, mockObjectSet.title, mockObjectSet.dataView),
            details = mockObjectSet.details
        )

        val unsupportedLayouts =
            ObjectType.Layout.entries - setOf(
                ObjectType.Layout.BASIC,
                ObjectType.Layout.TODO,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.IMAGE,
                ObjectType.Layout.FILE,
                ObjectType.Layout.VIDEO,
                ObjectType.Layout.AUDIO,
                ObjectType.Layout.PDF,
                ObjectType.Layout.BOOKMARK,
                ObjectType.Layout.PARTICIPANT,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.SET,
                ObjectType.Layout.COLLECTION,
                ObjectType.Layout.DATE,
                ObjectType.Layout.OBJECT_TYPE,
            )
        val unsupportedLayout = unsupportedLayouts.random()
        val objectUnsupportedLayout = ObjectWrapper.Basic(
            mapOf(
                Relations.ID to "objectUnsupportedLayout-${RandomString.make()}",
                Relations.SPACE_ID to mockObjectSet.spaceId,
                Relations.LAYOUT to unsupportedLayout.code.toDouble()
            )
        )

        stubSubscriptionResults(
            subscription = mockObjectSet.subscriptionId,
            spaceId = mockObjectSet.spaceId,
            storeOfRelations = storeOfRelations,
            keys = mockObjectSet.dvKeys,
            sources = listOf(mockObjectSet.setOf),
            objects = listOf(mockObjectSet.obj1, mockObjectSet.obj2, objectUnsupportedLayout),
            dependencies = listOf(),
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

            // CLICK ON RECORD NAME
            viewModel.onObjectHeaderClicked(objectUnsupportedLayout.id)

            // CHECK ERROR TOAST
            viewModel.toasts.test {
                val item = awaitItem()
                assertEquals("Unexpected layout: $unsupportedLayout", item)
            }
            // CHECK CLOSE BLOCK COMMAND NOT EMITTED
            advanceUntilIdle()
            verifyNoInteractions(closeObject)
        }
    }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart()
    }
}