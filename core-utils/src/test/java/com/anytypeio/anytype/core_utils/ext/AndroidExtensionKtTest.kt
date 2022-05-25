package com.anytypeio.anytype.core_utils.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidExtensionKtTest {

    @Test
    fun `should return file name with extension`() {

        val name = "Doc1"
        val mime = "application/pdf"

        val expected = "Doc1.pdf"
        val result = name.getFileName(mime)

        assertEquals(expected, result)
    }

    @Test
    fun `should return file name with wrong extension`() {

        val name = "Doc1"
        val mime = "applicationpdf"

        val expected = "Doc1.applicationpdf"
        val result = name.getFileName(mime)

        assertEquals(expected, result)
    }

    @Test
    fun `should return file name without extension`() {

        val name = "Doc1"
        val mime = null

        val expected = "Doc1"
        val result = name.getFileName(mime)

        assertEquals(expected, result)
    }
}