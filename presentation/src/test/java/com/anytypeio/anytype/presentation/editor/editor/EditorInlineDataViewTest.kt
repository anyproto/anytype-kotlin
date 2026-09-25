package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorInlineDataViewTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private val title = StubTitle()

    private val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should open type screen when inline query targets a type`() = runTest {
        val target = MockDataFactory.randomUuid()
        val dv = stubPageWithInlineDataView(
            target = target,
            targetLayout = ObjectType.Layout.OBJECT_TYPE
        )

        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        advanceUntilIdle()

        val observer = vm.navigation.test()

        vm.onClickListener(ListenerType.DataViewClick(target = dv.id))
        advanceUntilIdle()

        observer.assertValue { value ->
            value is EventWrapper && value.peekContent() == AppNavigation.Command.OpenTypeObject(
                target = target,
                space = defaultSpace
            )
        }
    }

    @Test
    fun `should open set screen when inline query targets a set`() = runTest {
        val target = MockDataFactory.randomUuid()
        val dv = stubPageWithInlineDataView(
            target = target,
            targetLayout = ObjectType.Layout.SET
        )
        stubClosePage()

        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        advanceUntilIdle()

        val observer = vm.navigation.test()

        vm.onClickListener(ListenerType.DataViewClick(target = dv.id))
        advanceUntilIdle()

        observer.assertValue { value ->
            value is EventWrapper && value.peekContent() == AppNavigation.Command.OpenSetOrCollection(
                target = target,
                space = defaultSpace
            )
        }
    }

    @Test
    fun `should show plural name when inline query targets a type`() = runTest {
        stubPageWithInlineDataView(
            target = MockDataFactory.randomUuid(),
            targetLayout = ObjectType.Layout.OBJECT_TYPE
        )

        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        advanceUntilIdle()

        val card = vm.views.filterIsInstance<BlockView.DataView>().single()
        assertEquals(expected = "Tasks", actual = card.title)
    }

    @Test
    fun `should show name when inline query targets a set`() = runTest {
        stubPageWithInlineDataView(
            target = MockDataFactory.randomUuid(),
            targetLayout = ObjectType.Layout.SET
        )

        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        advanceUntilIdle()

        val card = vm.views.filterIsInstance<BlockView.DataView>().single()
        assertEquals(expected = "Task", actual = card.title)
    }

    private fun stubPageWithInlineDataView(
        target: String,
        targetLayout: ObjectType.Layout
    ): Block {
        val dv = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                viewers = emptyList(),
                targetObjectId = target
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id, dv.id)
            ),
            header,
            title,
            dv
        )

        stubInterceptEvents()
        stubOpenDocument(
            document = page,
            details = ObjectViewDetails(
                mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.TYPE to listOf(objType.id)
                    ),
                    target to mapOf(
                        Relations.ID to target,
                        Relations.NAME to "Task",
                        Relations.PLURAL_NAME to "Tasks",
                        Relations.LAYOUT to targetLayout.code.toDouble(),
                        Relations.SPACE_ID to defaultSpace
                    )
                )
            )
        )

        return dv
    }
}
