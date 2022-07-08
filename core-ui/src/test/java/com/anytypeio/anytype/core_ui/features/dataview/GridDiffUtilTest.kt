package com.anytypeio.anytype.core_ui.features.dataview

import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertIs
import kotlin.test.assertNull

class GridDiffUtilTest {

    @Test
    fun `showIcon changed - header payload exists`() {
        val old = Viewer.GridView.Row(
            id = MockDataFactory.randomUuid(),
            showIcon = false
        )
        val new = old.copy(
            showIcon = true
        )
        val payload = ViewerGridAdapter.GridDiffUtil.getChangePayload(
            old, new
        )
        val payloadCasted = assertIs<List<Int>>(payload)

        assertContains(payloadCasted, ViewerGridAdapter.GridDiffUtil.OBJECT_HEADER_CHANGED)
    }

    @Test
    fun `nothing changed - payloads is empty`() {
        val old = Viewer.GridView.Row(
            id = MockDataFactory.randomUuid(),
            showIcon = false
        )
        val new = old.copy()
        val payload = ViewerGridAdapter.GridDiffUtil.getChangePayload(
            old, new
        )
        assertNull(payload)
    }
}