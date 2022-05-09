package com.anytypeio.anytype.domain.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.`object`.unset
import org.junit.Test
import kotlin.test.assertEquals

class ObjectWrapperExtTest {

    @Test
    fun `should update several fields with amend operation and preserve old fields - amend operation`() {

        val initial = ObjectWrapper.Basic(
            mapOf(
                Relations.NAME to "Friedrich Kittler",
                Relations.DONE to false,
                Relations.DESCRIPTION to null,
                Relations.IS_FAVORITE to false
            )
        )

        val expected = ObjectWrapper.Basic(
            mapOf(
                Relations.NAME to "Friedrich Kittler",
                Relations.DONE to true,
                Relations.DESCRIPTION to "German media philosopher",
                Relations.IS_FAVORITE to true
            )
        )

        val result = initial.amend(
            mapOf(
                Relations.DONE to true,
                Relations.IS_FAVORITE to true,
                Relations.DESCRIPTION to "German media philosopher",
            )
        )

        assertEquals(
            expected = expected.map,
            actual = result.map
        )
    }

    @Test
    fun `should remove several fields - unset operation`() {

        val firstRelationId = MockDataFactory.randomUuid()
        val firstRelationValue = MockDataFactory.randomString()
        val secondRelationId = MockDataFactory.randomUuid()
        val secondRelationValue = MockDataFactory.randomInt()
        val thirdRelationId = MockDataFactory.randomUuid()
        val thirdRelationValue = MockDataFactory.randomBoolean()

        val initial = ObjectWrapper.Basic(
            mapOf(
                Relations.NAME to "Friedrich Kittler",
                Relations.DONE to false,
                Relations.DESCRIPTION to null,
                Relations.IS_FAVORITE to false,
                firstRelationId to firstRelationValue,
                secondRelationId to secondRelationValue,
                thirdRelationId to thirdRelationValue
            )
        )

        val expected = ObjectWrapper.Basic(
            mapOf(
                Relations.NAME to "Friedrich Kittler",
                Relations.DONE to false,
                Relations.DESCRIPTION to null,
                Relations.IS_FAVORITE to false,
            )
        )

        val result = initial.unset(
            keys = listOf(
                firstRelationId,
                secondRelationId,
                thirdRelationId
            )
        )

        assertEquals(
            expected = expected.map,
            actual = result.map
        )
    }

    @Test
    fun `should move ids in two directions - to the left or to the right`() {

        val initial = listOf("1", "2", "3", "4", "5")

        val result1 = initial.move(
            target = initial.last(),
            afterId = null
        )

        assertEquals(
            expected = listOf("5", "1", "2", "3", "4"),
            actual = result1
        )

        val result2 = initial.move(
            target = "2",
            afterId = "3"
        )

        assertEquals(
            expected = listOf("1", "3", "2", "4", "5"),
            actual = result2
        )

        val result3 = initial.move(
            target = "1",
            afterId = "5"
        )

        assertEquals(
            expected = listOf("2", "3", "4", "5", "1"),
            actual = result3
        )

        val result4 = initial.move(
            target = "4",
            afterId = "2"
        )

        assertEquals(
            expected = listOf("1", "2", "4", "3", "5"),
            actual = result4
        )

        val result5 = initial.move(
            target = "5",
            afterId = "1"
        )

        assertEquals(
            expected = listOf("1", "5", "2", "3", "4"),
            actual = result5
        )
    }
}