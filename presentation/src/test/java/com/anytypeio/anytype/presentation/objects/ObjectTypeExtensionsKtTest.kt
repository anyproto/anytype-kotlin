package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.StubObjectType
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class ObjectTypeExtensionsTest {

    @Test
    fun `isTemplateAllowed returns true when type is not in getNoTemplates and recommendedLayout is in editorLayouts`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.PAGE,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertTrue(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is not in getNoTemplates and recommendedLayout is not in editorLayouts`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.PAGE,
            recommendedLayout = ObjectType.Layout.DASHBOARD.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is BOOKMARK`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.BOOKMARK,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is FILE`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.FILE,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is NOTE`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.NOTE,
            recommendedLayout = ObjectType.Layout.NOTE.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is SET`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.SET,
            recommendedLayout = ObjectType.Layout.SET.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is COLLECTION`() {
        val objectType = StubObjectType(
            id = ObjectTypeIds.COLLECTION,
            recommendedLayout = ObjectType.Layout.COLLECTION.code.toDouble()
        )
        val result = objectType.isTemplateAllowed()
        assertFalse(result)
    }
}
