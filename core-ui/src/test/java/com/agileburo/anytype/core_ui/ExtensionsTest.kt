package com.agileburo.anytype.core_ui

import com.agileburo.anytype.core_utils.ext.firstDigitByHash
import org.junit.Test
import kotlin.test.assertEquals

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
}