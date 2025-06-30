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

    @Test
    fun `should parse defaultTemplateId as single value from string`() {
        val templateId = MockDataFactory.randomString()

        assertEquals(
            expected = templateId,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to templateId
                )
            ).defaultTemplateId
        )
    }

    @Test  
    fun `should parse defaultTemplateId as single value from list`() {
        val templateId = MockDataFactory.randomString()

        assertEquals(
            expected = templateId,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to listOf(templateId)
                )
            ).defaultTemplateId
        )
    }

    @Test
    fun `should parse defaultTemplateId as single value from list with multiple items`() {
        val firstTemplateId = MockDataFactory.randomString()
        val secondTemplateId = MockDataFactory.randomString()

        assertEquals(
            expected = firstTemplateId,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to listOf(firstTemplateId, secondTemplateId)
                )
            ).defaultTemplateId
        )
    }

    @Test
    fun `should return null when defaultTemplateId is null`() {
        assertEquals(
            expected = null,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to null
                )
            ).defaultTemplateId
        )
    }

    @Test
    fun `should return null when defaultTemplateId is empty list`() {
        assertEquals(
            expected = null,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to emptyList<String>()
                )
            ).defaultTemplateId
        )
    }

    @Test
    fun `should return null when defaultTemplateId is missing from map`() {
        assertEquals(
            expected = null,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.NAME to "TestType"
                )
            ).defaultTemplateId
        )
    }

    @Test
    fun `should handle mixed type list and return first valid string for defaultTemplateId`() {
        val templateId = MockDataFactory.randomString()

        assertEquals(
            expected = templateId,
            actual = ObjectWrapper.Type(
                map = mapOf(
                    Relations.DEFAULT_TEMPLATE_ID to listOf(templateId, 123, null)
                )
            ).defaultTemplateId
        )
    }
}