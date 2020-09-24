package com.anytypeio.anytype

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
}