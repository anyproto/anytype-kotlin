package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.MockDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class RelationExtTest {

    @Test
    fun `should add ids to DVRecord`() {

        val relKey1 = MockDataFactory.randomUuid()
        val relKey2 = MockDataFactory.randomUuid()
        val relKey3 = MockDataFactory.randomUuid()

        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()

        val record = mapOf(
            relKey1 to MockDataFactory.randomString(),
            relKey2 to listOf<String>(id1, id2),
            relKey3 to MockDataFactory.randomInt()
        )

        val id3 = MockDataFactory.randomUuid()
        val id4 = MockDataFactory.randomUuid()

        val result = record.addIds(
            key = relKey2,
            ids = listOf(id3, id4)
        )

        val expected = mapOf(
            relKey1 to MockDataFactory.randomString(),
            relKey2 to listOf<String>(id1, id2, id3),
            relKey3 to MockDataFactory.randomInt()
        )

        assertEquals(expected, result)
    }
}