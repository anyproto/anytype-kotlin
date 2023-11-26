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
            uniqueKey = ObjectTypeIds.PAGE,
            recommendedLayout = ObjectType.Layout.BASIC.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertTrue(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is not in getNoTemplates and recommendedLayout is not in editorLayouts`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.PAGE,
            recommendedLayout = ObjectType.Layout.DASHBOARD.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is BOOKMARK`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.BOOKMARK,
            recommendedLayout = ObjectType.Layout.BOOKMARK.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is FILE`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.FILE,
            recommendedLayout = ObjectType.Layout.FILE.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is NOTE`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.NOTE,
            recommendedLayout = ObjectType.Layout.NOTE.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is SET`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.SET,
            recommendedLayout = ObjectType.Layout.SET.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is COLLECTION`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.COLLECTION,
            recommendedLayout = ObjectType.Layout.COLLECTION.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is IMAGE`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.IMAGE,
            recommendedLayout = ObjectType.Layout.IMAGE.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is VIDEO`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.VIDEO,
            recommendedLayout = ObjectType.Layout.VIDEO.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }

    @Test
    fun `isTemplateAllowed returns false when type is AUDIO`() {
        val objectType = StubObjectType(
            uniqueKey = ObjectTypeIds.AUDIO,
            recommendedLayout = ObjectType.Layout.AUDIO.code.toDouble()
        )
        val result = objectType.isTemplatesAllowed()
        assertFalse(result)
    }
}
