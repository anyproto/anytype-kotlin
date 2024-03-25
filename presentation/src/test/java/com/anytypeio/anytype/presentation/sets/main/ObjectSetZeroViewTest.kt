package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ObjectSetZeroViewTest : ObjectSetViewModelTestSetup() {

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

    private val objectSetDetails = Block.Details(
        mapOf(
            root to Block.Fields(
                mapOf(
                    Relations.SET_OF to listOf(MockDataFactory.randomUuid())
                )
            )
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not crash when trying to open relations, filters or sorts for data view containing no view`() {

        // SETUP

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                viewers = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubOpenObject(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = objectSetDetails
        )

        val vm = givenViewModel()

        // TESTING

        proceedWithStartingViewModel(vm)
        vm.onViewerCustomizeButtonClicked()
        vm.onViewerSortsClicked()
        vm.onViewerFiltersClicked()
        vm.onViewerSettingsClicked("")
    }

    private fun proceedWithStartingViewModel(vm: ObjectSetViewModel) {
        vm.onStart(ctx = root, space = defaultSpace)
    }
}