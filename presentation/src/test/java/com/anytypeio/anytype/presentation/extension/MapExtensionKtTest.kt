package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import org.junit.Test
import kotlin.test.assertEquals

class MapExtensionKtTest {

    @Test
    fun `should return updated map`() {

        val map = mapOf(
            "id1" to Block.Fields(mapOf("name" to "Name1", "size" to 111)),
            "id2" to Block.Fields(mapOf("name" to "Name2", "size" to 222)),
            "id3" to Block.Fields(mapOf("name" to "Name3", "size" to 333)),
            "id4" to Block.Fields(mapOf("name" to "Name4", "size" to 444)),
            "id5" to Block.Fields(mapOf("name" to "Name5", "size" to 555))
        )

        val update = mapOf(
            "id2" to Block.Fields(mapOf("name" to "Name9", "size" to 234)),
            "id4" to Block.Fields(mapOf("name" to "Name4", "size" to 874)),
            "id6" to Block.Fields(mapOf("name" to "Name6", "size" to 675))
        )

        val result = map.updateFields(update)

        val expected = mapOf(
            "id1" to Block.Fields(mapOf("name" to "Name1", "size" to 111)),
            "id2" to Block.Fields(mapOf("name" to "Name9", "size" to 234)),
            "id3" to Block.Fields(mapOf("name" to "Name3", "size" to 333)),
            "id4" to Block.Fields(mapOf("name" to "Name4", "size" to 874)),
            "id5" to Block.Fields(mapOf("name" to "Name5", "size" to 555)),
            "id6" to Block.Fields(mapOf("name" to "Name6", "size" to 675))
        )

        assertEquals(expected = expected, actual = result)
    }
}