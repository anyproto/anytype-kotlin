package com.anytypeio.anytype

import com.anytypeio.anytype.core_utils.ext.addAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.moveAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.swap
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {

    var list = mutableListOf(0, 1, 4, 9, 16, 25, 36, 49, 64, 81)
    var testList = mutableListOf(0, 49, 1, 4, 9, 16, 25, 36, 64, 81)
    var testList2 = mutableListOf(9, 81, 0, 1, 4, 16, 64, 25, 36, 49)

    @Test
    fun shouldDoProperSwap() {

        list.swap(1, 0)
        assertEquals(1, list[0])
        assertEquals(0, list[1])

        list.swap(5, 8)
        assertEquals(25, list[8])
        assertEquals(64, list[5])
    }

    @Test
    fun shouldShiftUp() {

        val shiftList = list.shift(1, 0)
        assertEquals(1, shiftList[0])

        val shiftList1 = list.shift(2, 0)
        assertEquals(4, shiftList1[0])

        val shiftList2 = list.shift(7, 1)
        assertEquals(shiftList2, testList)

        val shiftList3 = list
            .shift(9, 0)
            .shift(9, 6)
            .shift(4, 0)
        assertEquals(testList2, shiftList3)
    }

    @Test
    fun `when predicate index is true, should move all elements after this index`() {

        list.moveAfterIndexInLine(
            predicateIndex = { it == 9 },
            predicateMove = { i -> listOf(0, 1, 4, 36, 81).contains(i) }
        )

        val expected = mutableListOf(9, 0, 1, 4, 36, 81, 16, 25, 49, 64)

        assertEquals(expected, list)
    }

    @Test
    fun `when predicate index is false, should move all elements at the and`() {

        list.moveAfterIndexInLine(
            predicateIndex = { it == 91 },
            predicateMove = { i -> listOf(0, 1, 4, 36, 81).contains(i) }
        )

        val expected = mutableListOf(9, 16, 25, 49, 64, 0, 1, 4, 36, 81)

        assertEquals(expected, list)
    }

    @Test
    fun `when predicate index is true and predicateMove is false, should not change the list`() {

        list.moveAfterIndexInLine(
            predicateIndex = { it == 9 },
            predicateMove = { i -> listOf(10, 11, 14, 181).contains(i) }
        )

        val expected = mutableListOf(0, 1, 4, 9, 16, 25, 36, 49, 64, 81)

        assertEquals(expected, list)
    }

    @Test
    fun `when predicate index is true, should add elements after this index`() {

        list.addAfterIndexInLine(
            predicateIndex = { it == 9 },
            items = listOf(10, 11, 120),
        )

        val expected = mutableListOf(0, 1, 4, 9, 10, 11, 120, 16, 25, 36, 49, 64, 81)

        assertEquals(expected, list)
    }

    @Test
    fun `when predicate index is false, should add all items at the end`() {

        list.addAfterIndexInLine(
            predicateIndex = { it == 19 },
            items = listOf(10, 11, 120),
        )

        val expected = mutableListOf(0, 1, 4, 9, 16, 25, 36, 49, 64, 81, 10, 11, 120)

        assertEquals(expected, list)
    }
}