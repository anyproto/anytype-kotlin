package com.agileburo.anytype.domain.block.model

import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FieldsTest {

    @Test
    fun `should return null if isArchived-key is not present`() {
        val fields = Block.Fields(emptyMap())
        assertNull(actual = fields.isArchived)
    }

    @Test
    fun `should return value for isArchived property`() {
        Block.Fields(
            map = mapOf(
                Block.Fields.IS_ARCHIVED_KEY to false
            )
        ).apply {
            assertTrue { isArchived == false }
        }

        Block.Fields(
            map = mapOf(
                Block.Fields.IS_ARCHIVED_KEY to true
            )
        ).apply {
            assertTrue { isArchived == true }
        }
    }
}