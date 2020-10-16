package com.anytypeio.anytype.presentation.page.search

import org.junit.Test
import java.util.regex.Pattern
import kotlin.test.assertEquals

class DocumentSearchEngineTest {

    @Test
    fun `should find nothing`() {

        val text = "FooBarFoo"

        val query = "Far"

        val pattern = Pattern.compile(query, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

        val result = text.search(pattern)

        assertEquals(
            expected = setOf(),
            actual = result
        )
    }

    @Test
    fun `should find 'foo' twice at the start and at the end`() {

        val text = "FooBarFoo"

        val query = "Foo"

        val pattern = Pattern.compile(query, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

        val result = text.search(pattern)

        assertEquals(
            expected = setOf(0..3, 6..9),
            actual = result
        )
    }

    @Test
    fun `should find 'bar' once in the middle`() {

        val text = "FooBarFoo"

        val query = "Bar"

        val pattern = Pattern.compile(query, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

        val result = text.search(pattern)

        assertEquals(
            expected = setOf(3..6),
            actual = result
        )
    }

    @Test
    fun `should find 'bar' surrounded by empty spaces only once in the middle`() {

        val text = "Foo Bar Foo"

        val query = "Bar"

        val pattern = Pattern.compile(query, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

        val result = text.search(pattern)

        assertEquals(
            expected = setOf(4..7),
            actual = result
        )
    }

    @Test
    fun `should find 'f' in all words case-insensitive`() {

        val text = "Five fast flying machines"

        val query = "f"

        val pattern = Pattern.compile(query, Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

        val result = text.search(pattern)

        assertEquals(
            expected = setOf(0..1, 5..6, 10..11),
            actual = result
        )
    }
}