package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.isWhole
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtensionsTest {

    @Test
    fun `should return one digit by non null string`() {

        val name = "Alex"

        val result = name.firstDigitByHash()

        assertEquals(expected = 2, actual = result)
    }

    @Test
    fun `should return zero by empty string`() {

        val name = ""

        val result = name.firstDigitByHash()

        assertEquals(expected = 0, actual = result)
    }

    @Test
    fun `should be whole double`() {
        val d = 654.0

        val result = d.isWhole()

        assertTrue(result)
    }

    @Test
    fun `should not be negative whole double`() {
        val d = -555.0

        val result = d.isWhole()

        assertTrue(result)
    }

    @Test
    fun `should not be whole double`() {
        val d = 654.1

        val result = d.isWhole()

        assertFalse(result)
    }
}