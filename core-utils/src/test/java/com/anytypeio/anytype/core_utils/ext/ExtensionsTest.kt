package com.anytypeio.anytype.core_utils.ext

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFalse

class ExtensionsTest {

    @Test
    fun shouldUpdateAllItemsInMutableList() {

        val mutableList = mutableListOf(10, 12, 14, 16, 20)

        mutableList.mapInPlace { num -> num + 3 }

        val expected = mutableListOf(13, 15, 17, 19, 23)

        mutableList.forEachIndexed { index, i ->
            assertEquals(expected[index], i)
        }
    }

    @Test
    fun shouldNotUpdateItemsInEmptyMutableList() {

        val mutableList = mutableListOf<Int>()

        mutableList.mapInPlace { num -> num + 3 }

        mutableList.forEachIndexed { index, i ->
            assertFalse(true)
        }
    }
}