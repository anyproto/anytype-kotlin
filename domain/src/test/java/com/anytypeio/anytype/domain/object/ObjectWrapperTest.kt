package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectWrapperTest {
    
    @Test
    fun `should parse description as single value`() {
        
        val description = MockDataFactory.randomString()

        assertEquals(
            expected = description,
            actual = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.DESCRIPTION to description
                )
            ).description
        )

        assertEquals(
            expected = description,
            actual = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.DESCRIPTION to listOf(description)
                )
            ).description
        )

        assertEquals(
            expected = null,
            actual = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.DESCRIPTION to null
                )
            ).description
        )
    }
}