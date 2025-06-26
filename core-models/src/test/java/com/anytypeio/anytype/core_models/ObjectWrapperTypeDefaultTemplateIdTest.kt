package com.anytypeio.anytype.core_models

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import java.util.UUID

/**
 * Tests for DROID-3763 fix: ClassCastException in ObjectWrapper.Type.defaultTemplateId
 * 
 * This test class specifically addresses the crash that occurred when the backend
 * returned a List for defaultTemplateId field instead of a single String value.
 * 
 * Original error: java.util.Collections$UnmodifiableRandomAccessList cannot be cast to java.lang.String
 */
class ObjectWrapperTypeDefaultTemplateIdTest {

    @Test
    fun `should not crash when defaultTemplateId is provided as UnmodifiableList`() {
        val templateId = UUID.randomUUID().toString()
        
        // This simulates the exact crash scenario where backend returns an UnmodifiableList
        val unmodifiableList = java.util.Collections.unmodifiableList(listOf<String>(templateId))
        
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to unmodifiableList
            )
        )
        
        // This should not throw ClassCastException anymore
        assertEquals(templateId, objectType.defaultTemplateId)
    }

    @Test
    fun `should handle ArrayList for defaultTemplateId`() {
        val templateId = UUID.randomUUID().toString()
        
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to arrayListOf(templateId)
            )
        )
        
        assertEquals(templateId, objectType.defaultTemplateId)
    }

    @Test
    fun `should handle mutableList for defaultTemplateId`() {
        val templateId = UUID.randomUUID().toString()
        
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to mutableListOf(templateId)
            )
        )
        
        assertEquals(templateId, objectType.defaultTemplateId)
    }

    @Test
    fun `should handle single string value for defaultTemplateId`() {
        val templateId = UUID.randomUUID().toString()
        
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to templateId
            )
        )
        
        assertEquals(templateId, objectType.defaultTemplateId)
    }

    @Test
    fun `should return first element from multi-item list for defaultTemplateId`() {
        val firstTemplateId = UUID.randomUUID().toString()
        val secondTemplateId = UUID.randomUUID().toString()
        
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to listOf(firstTemplateId, secondTemplateId)
            )
        )
        
        assertEquals(firstTemplateId, objectType.defaultTemplateId)
    }

    @Test
    fun `should return null for empty list defaultTemplateId`() {
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to emptyList<String>()
            )
        )
        
        assertNull(objectType.defaultTemplateId)
    }

    @Test
    fun `should return null for null defaultTemplateId`() {
        val objectType = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.DEFAULT_TEMPLATE_ID to null
            )
        )
        
        assertNull(objectType.defaultTemplateId)
    }

    @Test
    fun `should return null when defaultTemplateId is missing from map`() {
        val objectType = ObjectWrapper.Type(
            map = mapOf(
                Relations.ID to UUID.randomUUID().toString(),
                Relations.NAME to "Test Type"
            )
        )
        
        assertNull(objectType.defaultTemplateId)
    }

    @Test
    fun `should work correctly in mapper scenario that caused original crash`() {
        val templateId = UUID.randomUUID().toString()
        val typeId = UUID.randomUUID().toString()
        val typeName = "Test Object Type"
        
        // Simulate the exact data structure that caused the crash
        val objectType = ObjectWrapper.Type(
            map = mapOf(
                Relations.ID to typeId,
                Relations.NAME to typeName,
                Relations.UNIQUE_KEY to "testKey",
                Relations.DEFAULT_TEMPLATE_ID to java.util.Collections.unmodifiableList(listOf(templateId))
            )
        )
        
        // This should work without throwing ClassCastException
        // This is the exact line from MapperExtension.kt that was crashing
        val defaultTemplate = objectType.defaultTemplateId
        
        assertEquals(templateId, defaultTemplate)
    }
}