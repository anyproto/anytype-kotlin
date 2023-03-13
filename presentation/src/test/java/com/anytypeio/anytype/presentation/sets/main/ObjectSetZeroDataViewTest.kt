package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ObjectSetZeroDataViewTest : ObjectSetViewModelTestSetup() {

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
    fun `should not customize-view panel if no data view is found`() {

        // SETUP

        stubInterceptEvents()
        stubOpenObject(
            doc = listOf(
                header,
                title
            )
        )

        val vm = givenViewModel()

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
        stubOpenObject(
            doc = listOf(
                header,
                title
            )
        )

        val vm = givenViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)
        vm.onExpandViewerMenuClicked()
    }
}