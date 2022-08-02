package com.anytypeio.anytype.core_ui.features.dv

import com.anytypeio.anytype.core_ui.features.dataview.diff.CellViewDiffUtil
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import kotlin.test.assertEquals

class CellViewDiffUtilTest {

    @Test
    fun `should detect content changed for number cell`() {
        val index = 0

        val oldCell = CellView.Number(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            number = MockDataFactory.randomString(),
        )

        val newCell = oldCell.copy(
            number = MockDataFactory.randomString()
        )

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content did not change for number cell`() {
        val index = 0

        val oldCell = CellView.Number(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            number = MockDataFactory.randomString(),
        )

        val newCell = oldCell.copy()

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = true, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content changed for description cell`() {
        val index = 0

        val oldCell = CellView.Description(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString()
        )

        val newCell = oldCell.copy(
            text = MockDataFactory.randomString()
        )

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content did not change for description cell`() {
        val index = 0

        val oldCell = CellView.Description(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            text = MockDataFactory.randomString()
        )

        val newCell = oldCell.copy()

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = true, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content changed for date cell`() {
        val index = 0

        val oldCell = CellView.Date(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            timeInSecs = MockDataFactory.randomLong(),
            dateFormat = MockDataFactory.randomString()
        )

        val newCell = oldCell.copy(
            timeInSecs = MockDataFactory.randomLong()
        )

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = differ.areContentsTheSame(index, index))
    }

    @Test
    fun `should detect content did not change for date cell`() {
        val index = 0

        val oldCell = CellView.Date(
            id = MockDataFactory.randomString(),
            key = MockDataFactory.randomString(),
            dateFormat = MockDataFactory.randomString()
        )

        val newCell = oldCell.copy()

        val old = listOf(oldCell)
        val new = listOf(newCell)

        val differ = CellViewDiffUtil(old, new)

        assertEquals(expected = true, actual = differ.areItemsTheSame(index, index))
        assertEquals(expected = true, actual = differ.areContentsTheSame(index, index))
    }
}