package com.anytypeio.anytype.domain.block.model

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relations
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
                Relations.IS_ARCHIVED to false
            )
        ).apply {
            assertTrue { isArchived == false }
        }

        Block.Fields(
            map = mapOf(
                Relations.IS_ARCHIVED to true
            )
        ).apply {
            assertTrue { isArchived == true }
        }
    }
}