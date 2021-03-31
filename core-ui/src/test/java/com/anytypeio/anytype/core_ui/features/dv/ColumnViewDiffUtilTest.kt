package com.anytypeio.anytype.core_ui.features.dv

import com.anytypeio.anytype.core_ui.MockDataFactory
import com.anytypeio.anytype.core_ui.features.dataview.diff.ColumnViewDiffUtil
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import org.junit.Test
import kotlin.test.assertEquals

class ColumnViewDiffUtilTest {

    @Test
    fun `should detect content changed for column`() {
        val index = 0

        val oldColumn = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.values().random(),
            isVisible = MockDataFactory.randomBoolean(),
            isHidden = MockDataFactory.randomBoolean(),
            isReadOnly = MockDataFactory.randomBoolean(),
            width = MockDataFactory.randomInt()
        )

        val newColumn = oldColumn.copy(
            text = MockDataFactory.randomString()
        )

        val old = listOf(oldColumn)
        val new = listOf(newColumn)

        val differ = ColumnViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content does not changed for column`() {
        val index = 0

        val oldColumn = ColumnView(
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString(),
            format = ColumnView.Format.values().random(),
            isVisible = MockDataFactory.randomBoolean(),
            isHidden = MockDataFactory.randomBoolean(),
            isReadOnly = MockDataFactory.randomBoolean(),
            width = MockDataFactory.randomInt()
        )

        val newColumn = oldColumn.copy()

        val old = listOf(oldColumn)
        val new = listOf(newColumn)

        val differ = ColumnViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = true, actual = differ.areContentsTheSame(index, index))
    }
}