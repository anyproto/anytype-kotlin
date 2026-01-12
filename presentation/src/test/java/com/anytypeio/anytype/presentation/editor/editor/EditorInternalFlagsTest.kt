package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.toObject
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

@ExperimentalCoroutinesApi
class EditorInternalFlagsTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should has all internal flags on object open`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()
        stubGetObjectTypes(emptyList())

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.ID to root,
                        Relations.TYPE to ObjectTypeIds.NOTE,
                        Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(2.0, 0.0, 1.0)
                    )
                )
        )
        stubOpenDocument(document = document, details = detailsList)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val storedDetails = orchestrator.stores.details.current()

        val objectDetails = storedDetails.details[root].toObject()

        val expectedFlags = listOf(
            InternalFlags.ShouldSelectTemplate,
            InternalFlags.ShouldEmptyDelete,
            InternalFlags.ShouldSelectType
        )
        val actualFlags = objectDetails?.internalFlags

        assertEquals(expected = expectedFlags, actual = actualFlags)
    }

    @Test
    fun `should not remove type flag on show object event with type flag in details`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.PAGE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(
                            InternalFlags.ShouldSelectTemplate.code.toDouble(),
                            InternalFlags.ShouldEmptyDelete.code.toDouble(),
                            InternalFlags.ShouldSelectType.code.toDouble(),
                        )
                    )

            )
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()

        val vm = buildViewModel()

        stubFileLimitEvents()
        stubSetInternalFlags()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        verifyNoInteractions(setObjectInternalFlags)

        coroutineTestRule.advanceTime(100)
    }

    @Test
    fun `should not remove type flag on show object event without type flag in details`() = runTest {

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)

        stubInterceptEvents()
        stubInterceptThreadStatus()

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.PAGE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(
                            InternalFlags.ShouldSelectTemplate.code.toDouble(),
                            InternalFlags.ShouldEmptyDelete.code.toDouble()
                        )
                    )

            )
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()

        val vm = buildViewModel()

        stubFileLimitEvents()
        stubSetInternalFlags()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        verifyNoInteractions(setObjectInternalFlags)

        advanceUntilIdle()
    }

    @Test
    fun `should not remove template flag on start template selection widget when flag isn't present`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.PAGE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(
                            InternalFlags.ShouldSelectType.code.toDouble(),
                            InternalFlags.ShouldEmptyDelete.code.toDouble(),
                        )
                    )

            )
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()

        val vm = buildViewModel()

        stubFileLimitEvents()
        stubSetInternalFlags()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        verifyNoInteractions(setObjectInternalFlags)

        advanceUntilIdle()

        verifyNoMoreInteractions(setObjectInternalFlags)

        coroutineTestRule.advanceTime(100)
    }

    @Test
    fun `should doesn't show type widget when flag is present and title is not empty`() = runTest {
        val title = StubTitle(text = "Some text")
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.PAGE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(
                            InternalFlags.ShouldSelectType.code.toDouble(),
                        )
                    )

            )
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()
        stubFileLimitEvents()
        stubSetInternalFlags()

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.typesWidgetState.test{
            val first = awaitItem()
            assertEquals(EditorViewModel.TypesWidgetState(
                items = emptyList(),
                visible = false,
                expanded = false
            ), first)
            vm.onStart(id = root, space = defaultSpace)
            advanceUntilIdle()
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `should show type widget when flag is present and title is empty`() = runTest {
        val title = StubTitle(text = "")
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = ObjectViewDetails(
            details = mapOf(
                root to
                    mapOf(
                        Relations.ID to root,
                        Relations.TYPE to ObjectTypeIds.PAGE,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(
                            InternalFlags.ShouldSelectType.code.toDouble(),
                        )
                    )

            )
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()
        stubFileLimitEvents()
        stubSetInternalFlags()

        val vm = buildViewModel()

        // Set store after buildViewModel to override the default objType
        setStoreOfObjectTypes(listOf(StubObjectType(
            id = ObjectTypeIds.PAGE,
            uniqueKey = ObjectTypeIds.PAGE,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )))
        advanceUntilIdle()

        vm.typesWidgetState.test{
            val first = awaitItem()
            assertEquals(EditorViewModel.TypesWidgetState(
                items = listOf(),
                visible = false,
                expanded = false
            ), first)
            vm.onStart(id = root, space = defaultSpace)
            advanceUntilIdle()
            val second = awaitItem()
            assertEquals(EditorViewModel.TypesWidgetState(
                items = listOf(),
                visible = true,
                expanded = false
            ), second)
            val third = awaitItem()
            assertEquals(EditorViewModel.TypesWidgetState(
                items = listOf(EditorViewModel.TypesWidgetItem.Search),
                visible = true,
                expanded = false
            ), third)
            ensureAllEventsConsumed()
        }
    }
}