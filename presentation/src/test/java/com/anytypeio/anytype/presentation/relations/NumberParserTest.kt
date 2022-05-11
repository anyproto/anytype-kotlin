package com.anytypeio.anytype.presentation.relations

import com.anytypeio.anytype.presentation.number.NumberParser
import org.junit.Test
import kotlin.test.assertEquals

class NumberParserTest {

    @Test
    fun `whole number - shown without point`() {
        assertEquals("1", NumberParser.parse("1.00"))
        assertEquals("1", NumberParser.parse(1.00))
    }

    @Test
    fun `two digits after dot - shown with two digits after dot`() {
        assertEquals("1.11", NumberParser.parse("1.11"))
        assertEquals("1.11", NumberParser.parse(1.11))
    }

    @Test
    fun `two digits after dot with zero at the end - shown as one digit after dot`() {
        assertEquals("1.1", NumberParser.parse("1.10"))
        assertEquals("1.1", NumberParser.parse(1.10))
    }

    @Test
    fun `big number whole number`() {
        assertEquals("3757556070", NumberParser.parse("3757556070"))
        assertEquals("3757556070", NumberParser.parse(3757556070))
    }

    @Test
    fun `big number real number`() {
        assertEquals("3757556070.1", NumberParser.parse("3757556070.1"))
        assertEquals("3757556070.1", NumberParser.parse(3757556070.1))
    }


    @Test
    fun `many digits after dot`() {
        assertEquals("1.11111111111115", NumberParser.parse("1.11111111111115"))
        assertEquals("1.11111111111115", NumberParser.parse(1.11111111111115))
    }
}