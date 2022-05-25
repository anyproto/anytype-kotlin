package com.anytypeio.anytype.core_ui.tools

import org.junit.Test
import kotlin.test.assertEquals

class SlashHelperTest {

    @Test
    fun `should be position 5 when slash char is added`() {

        val text = "test /"
        val start = 5
        val count = 1

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = 5

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be position 7 when slash char is added`() {

        val text = "test fo/"
        val start = 5
        val count = 3

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = 7

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be position 4 when slash char is added`() {

        val text = "test/"
        val start = 0
        val count = 5

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = 4

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be position 11 when slash char is added`() {

        val text = "test/foobar/"
        val start = 5
        val count = 7

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = 11

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be no position when slash char is removed 1`() {

        val text = "test"
        val start = 4
        val count = 0

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = SlashTextWatcher.NO_SLASH_POSITION

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be no position when slash char is removed 2`() {

        val text = "test/foobar"
        val start = 11
        val count = 0

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = SlashTextWatcher.NO_SLASH_POSITION

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be no position when slash char is removed 3`() {

        val text = "test/"
        val start = 5
        val count = 0

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = SlashTextWatcher.NO_SLASH_POSITION

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be no position when slash char is removed 4`() {

        val text = "test/fooba"
        val start = 5
        val count = 5

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = SlashTextWatcher.NO_SLASH_POSITION

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should be no position when char position is out of bound`() {

        val text = "test/"
        val start = 4
        val count = 2

        val result = SlashHelper.getSlashPosition(
            text = text, start = start, count = count
        )

        val expected = SlashTextWatcher.NO_SLASH_POSITION

        assertEquals(expected = expected, actual = result)
    }

}

