package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetZeroDataViewTest : ObjectSetViewModelTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
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
    }

    @Test
    fun `should show empty-source error`() {

        // SETUP

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            )
        )

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)

        assertEquals(
            expected = ObjectSetViewModel.OBJECT_SET_HAS_EMPTY_SOURCE_ERROR,
            actual = vm.error.value
        )
    }

    @Test
    fun `should show no-content error because of missing dv`() {

        // SETUP

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            ),
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf(
                            Relations.SET_OF to listOf(MockDataFactory.randomUuid())
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)

        assertEquals(
            expected = ObjectSetViewModel.DATA_VIEW_NOT_FOUND_ERROR,
            actual = vm.error.value
        )
    }

    @Test
    fun `should not customize-view panel if no data view is found`() {

        // SETUP

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            )
        )

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)

        assertEquals(
            expected = false,
            actual = vm.isCustomizeViewPanelVisible.value
        )

        vm.onViewerCustomizeButtonClicked()

        assertEquals(
            expected = false,
            actual = vm.isCustomizeViewPanelVisible.value
        )
    }

    @Test
    fun `should not crash when change-active-view panel clicked when object set does not have data view`() {

        // SETUP

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title
            )
        )

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)
        vm.onExpandViewerMenuClicked()
    }
}