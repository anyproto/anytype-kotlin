package com.anytypeio.anytype.domain.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.amend
import com.anytypeio.anytype.domain.`object`.unset
import com.anytypeio.anytype.domain.common.MockDataFactory
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
}