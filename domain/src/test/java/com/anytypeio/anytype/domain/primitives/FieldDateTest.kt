package com.anytypeio.anytype.domain.primitives

import com.anytypeio.anytype.core_models.primitives.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FieldDateTest {

    @Test
    fun `parse should return Single when input is valid String number`() {
        val input = "1627814400"
        val expected = Value.Single(1627814400L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }

    @Test
    fun `parse should return null when input is invalid String`() {
        val input = "invalid_number"

        val result = FieldDateParser.parse(input)

        assertNull(result)
    }

    @Test
    fun `parse should return Single when input is Number`() {
        val input = 1627814400L
        val expected = Value.Single(1627814400L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }

    @Test
    fun `parse should return Single of first valid value when input is List`() {
        val input = listOf("invalid_number", "1627814400", 1627900800L)
        val expected = Value.Single(1627814400L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }

    @Test
    fun `parse should return Single when input is List with one valid value`() {
        val input = listOf("1627814400")
        val expected = Value.Single(1627814400L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }

    @Test
    fun `parse should return null when List contains no valid entries`() {
        val input = listOf("invalid_number", null, "another_invalid")

        val result = FieldDateParser.parse(input)

        assertNull(result)
    }

    @Test
    fun `parse should return Single when input is List of Numbers and Strings`() {
        val input = listOf(1627900800L, "1627814400")
        val expected = Value.Single(1627900800L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }

    @Test
    fun `parse should return null when input is null`() {
        val input = null

        val result = FieldDateParser.parse(input)

        assertNull(result)
    }

    @Test
    fun `parse should return null when input is unsupported type`() {
        val input = mapOf("key" to "value")

        val result = FieldDateParser.parse(input)

        assertNull(result)
    }

    @Test
    fun `parse should handle floating-point numbers in List by converting to Long`() {
        val input = listOf(1627814400.99, "invalid_number")
        val expected = Value.Single(1627814400L)

        val result = FieldDateParser.parse(input)

        assertEquals(expected, result)
    }
}