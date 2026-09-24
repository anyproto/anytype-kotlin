package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class EditorInlineSetTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.logAllWhenTestFails()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should open the page at the inline block when an inline set is clicked`() = runTest {
        val targetSet = MockDataFactory.randomUuid()
        val title = StubTitle()
        val dataView = StubDataView(targetObjectId = targetSet)
        val page = StubSmartBlock(id = root, children = listOf(title.id, dataView.id))

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubClosePage()
        stubOpenDocument(
            document = listOf(page, title, dataView),
            details = ObjectViewDetails(
                details = mapOf(
                    root to mapOf(
                        Relations.ID to root,
                        Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                    ),
                    targetSet to mapOf(
                        Relations.ID to targetSet,
                        Relations.SPACE_ID to defaultSpace,
                        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble()
                    )
                )
            )
        )

        val vm = buildViewModel()
        vm.onStart(id = root, space = defaultSpace)
        advanceUntilIdle()

        val testObserver = vm.navigation.test()

        vm.onClickListener(ListenerType.DataViewClick(target = dataView.id))
        advanceUntilIdle()

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == AppNavigation.Command.OpenSetOrCollection(
                target = root,
                space = defaultSpace,
                dataViewBlockId = dataView.id
            )
        }
    }
}
