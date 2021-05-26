package com.anytypeio.anytype.presentation.page.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorStartupTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should not crash in process rendering when root block for current context isn't loaded`() {

        // SETUP

        val doc = listOf(
            Block(
                id = "some id",
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart(),
                children = emptyList()
            )
        )

        stubOpenDocument(document = doc)
        stubInterceptEvents()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.state.test().assertValue { state ->
            state is ViewState.Success && state.blocks.isEmpty()
        }
    }

}