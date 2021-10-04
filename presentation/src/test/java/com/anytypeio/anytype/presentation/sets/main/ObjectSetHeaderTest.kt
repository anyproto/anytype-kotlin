package com.anytypeio.anytype.presentation.sets.main

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ObjectSetHeaderTest : ObjectSetViewModelTestSetup() {

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

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should return header with title but without emoji`() {

        // SETUP

        val viewer = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = emptyList()
        )

        val dv = Block(
            id = MockDataFactory.randomUuid(),
            content = DV(
                sources = listOf(MockDataFactory.randomString()),
                relations = emptyList(),
                viewers = listOf(viewer)
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
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        assertEquals(
            expected = title.id,
            actual = vm.header.value?.id
        )

        assertEquals(
            expected = null,
            actual = vm.header.value?.emoji
        )
    }

    @Test
    fun `should return header with title but with emoji`() {

        // SETUP

        val emoji = MockDataFactory.randomString()

        val viewer = DVViewer(
                id = MockDataFactory.randomUuid(),
                filters = emptyList(),
                sorts = emptyList(),
                type = Block.Content.DataView.Viewer.Type.GRID,
                name = MockDataFactory.randomString(),
                viewerRelations = emptyList()
        )

        val dv = Block(
                id = MockDataFactory.randomUuid(),
                content = DV(
                        sources = listOf(MockDataFactory.randomString()),
                        relations = emptyList(),
                        viewers = listOf(viewer)
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
            details = Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf(
                            "iconEmoji" to emoji
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        assertEquals(
                expected = title.id,
                actual = vm.header.value?.id
        )

        assertEquals(
                expected = emoji,
                actual = vm.header.value?.emoji
        )
    }
}