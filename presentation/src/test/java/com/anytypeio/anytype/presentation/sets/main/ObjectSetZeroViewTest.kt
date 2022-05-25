package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetZeroViewTest : ObjectSetViewModelTestSetup() {

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
    fun `should show error if data view contains no view`() {

        // SETUP

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = objectSetDetails
        )

        val vm = buildViewModel()

        // TESTING

        assertEquals(
            expected = null,
            actual = vm.error.value
        )

        vm.onStart(root)

        assertEquals(
            expected = ObjectSetViewModel.DATA_VIEW_HAS_NO_VIEW_MSG,
            actual = vm.error.value
        )
    }

    @Test
    fun `should not crash when trying to open relations, filters or sorts for data view containing no view`() {

        // SETUP

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        stubInterceptEvents()
        stubOpenObjectSet(
            doc = listOf(
                header,
                title,
                dv
            ),
            details = objectSetDetails
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)
        vm.onViewerCustomizeButtonClicked()
        vm.onViewerSortsClicked()
        vm.onViewerFiltersClicked()
        vm.onViewerRelationsClicked()
    }
}