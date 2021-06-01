package com.anytypeio.anytype.domain.ext

import com.anytypeio.anytype.core_models.ext.replaceRangeWithWord
import org.junit.Test
import kotlin.test.assertEquals

class BlockSlashUpdateText {

    val EMPTY_STRING = ""

    @Test
    fun `should remove slashTrigger 1`() {

        val slashTrigger = "/"
        val given = "/"
        val from = 0
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = ""

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 2`() {

        val slashTrigger = "/"
        val given = "r/"
        val from = 1
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "r"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 3`() {

        val slashTrigger = "/"
        val given = "r/z"
        val from = 1
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "rz"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 4`() {

        val slashTrigger = "/"
        val given = "r/ z"
        val from = 1
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "r z"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 5`() {

        val slashTrigger = "/"
        val given = "r / z"
        val from = 2
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "r  z"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 6`() {

        val slashTrigger = "/x"
        val given = "/x"
        val from = 0
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = ""

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 7`() {

        val slashTrigger = "/x"
        val given = "r/x"
        val from = 1
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "r"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should remove slashTrigger 8`() {

        val slashTrigger = "/xy"
        val given = "r /xyz"
        val from = 2
        val to = from + slashTrigger.length

        val result = given.replaceRangeWithWord(
            replace = EMPTY_STRING,
            from = from,
            to = to
        )

        val expected = "r z"

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}