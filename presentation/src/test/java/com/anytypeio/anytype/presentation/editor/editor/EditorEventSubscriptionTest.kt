package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class EditorEventSubscriptionTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
        stubAnalyticSpaceHelperDelegate()
    }

    @Test
    fun `should subscribe only on start`() = runTest {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        val params = InterceptEvents.Params(context = root)

        stubOpenDocument(document = document)
        stubInterceptEvents(params)

        val vm = buildViewModel()

        // TESTING

        verifyNoInteractions(interceptEvents)

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        verify(interceptEvents, times(1)).build(params)

        vm.onStop()

        advanceUntilIdle()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        verify(interceptEvents, times(2)).build(params)
    }
}