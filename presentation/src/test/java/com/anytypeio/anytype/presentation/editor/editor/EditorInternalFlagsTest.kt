package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.domain.`object`.SetObjectInternalFlags
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@ExperimentalCoroutinesApi
class EditorInternalFlagsTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should has all internal flags on object open`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = Block.Details(
            details = mapOf(
                root to Block.Fields(
                    mapOf(
                        Relations.TYPE to ObjectTypeIds.NOTE,
                        Relations.LAYOUT to ObjectType.Layout.NOTE.code.toDouble(),
                        Relations.INTERNAL_FLAGS to listOf(2.0, 0.0, 1.0)
                    )
                )
            )
        )
        stubOpenDocument(document = document, details = detailsList)

        val vm = buildViewModel()

        vm.onStart(root)

        advanceUntilIdle()

        val storedDetails = orchestrator.stores.details.current()

        val objectDetails = ObjectWrapper.Basic(storedDetails.details[root]?.map.orEmpty())

        val expectedFlags = listOf(
            InternalFlags.ShouldSelectTemplate,
            InternalFlags.ShouldEmptyDelete,
            InternalFlags.ShouldSelectType
        )
        val actualFlags = objectDetails.internalFlags

        assertEquals(expected = expectedFlags, actual = actualFlags)
    }

    @Test
    fun `should hasn't internal flags on object open`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()
        stubOpenDocument(document = document)

        val vm = buildViewModel()

        vm.onStart(root)

        advanceUntilIdle()

        val storedDetails = orchestrator.stores.details.current()

        val objectDetails = ObjectWrapper.Basic(storedDetails.details[root]?.map.orEmpty())

        val expectedFlags = emptyList<InternalFlags>()
        val actualFlags = objectDetails.internalFlags

        assertEquals(expected = expectedFlags, actual = actualFlags)
    }

    @Test
    fun `should remove type flag on object type widget hide event`() = runTest {
        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val page = StubSmartBlock(id = root, children = listOf(header.id))
        val document = listOf(page, header, title)
        stubInterceptEvents()

        val detailsList = Block.Details(
            details = mapOf(
                root to Block.Fields(
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
        )
        stubOpenDocument(document = document, details = detailsList)
        stubGetObjectTypes(types = emptyList())
        stubGetDefaultObjectType()

        val vm = buildViewModel()

        stubFileLimitEvents()
        stubSetInternalFlags()

        vm.onStart(root)

        advanceUntilIdle()
        vm.onObjectTypesWidgetDoneClicked()

        advanceUntilIdle()

        verifyBlocking(setObjectInternalFlags, times(1)) {
            async(
                params = SetObjectInternalFlags.Params(
                    ctx = root,
                    flags = listOf(
                        InternalFlags.ShouldSelectTemplate,
                        InternalFlags.ShouldEmptyDelete
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(100)
    }
}