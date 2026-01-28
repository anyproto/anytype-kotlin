package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class ObjectWrapperBasicNavigationTest {
    private fun basicObj(
        id: String = "id1",
        space: String = "space1",
        layout: ObjectType.Layout? = null,
        source: String? = null,
        identityProfileLink: String? = null
    ): ObjectWrapper.Basic {
        val map = mutableMapOf<String, Any?>(
            Relations.ID to id,
            Relations.SPACE_ID to space
        )
        if (layout != null) map[Relations.LAYOUT] = layout.code.toDouble()
        if (source != null) map[Relations.SOURCE] = source
        if (identityProfileLink != null) map[Relations.IDENTITY_PROFILE_LINK] = identityProfileLink
        return ObjectWrapper.Basic(map)
    }

    @Test
    fun `bookmark with url navigates to OpenBookmarkUrl`() {
        val obj = basicObj(layout = ObjectType.Layout.BOOKMARK, source = "https://anytype.io")
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenBookmarkUrl)
        assertEquals("https://anytype.io", nav.url)
    }

    @Test
    fun `bookmark without url navigates to OpenEditor`() {
        val obj = basicObj(layout = ObjectType.Layout.BOOKMARK)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
        assertEquals("id1", nav.target)
    }

    @Test
    fun `basic layout navigates to OpenEditor`() {
        val obj = basicObj(layout = ObjectType.Layout.BASIC)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
    }

    @Test
    fun `note layout navigates to OpenEditor`() {
        val obj = basicObj(layout = ObjectType.Layout.NOTE)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
    }

    @Test
    fun `todo layout navigates to OpenEditor`() {
        val obj = basicObj(layout = ObjectType.Layout.TODO)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
    }

    @Test
    fun `file layout navigates to OpenEditor`() {
        val obj = basicObj(layout = ObjectType.Layout.FILE)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
    }

    @Test
    fun `profile layout with identityProfileLink navigates to OpenEditor with link`() {
        val obj = basicObj(layout = ObjectType.Layout.PROFILE, identityProfileLink = "profileId")
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
        assertEquals("profileId", nav.target)
    }

    @Test
    fun `profile layout without identityProfileLink navigates to OpenEditor with self id`() {
        val obj = basicObj(layout = ObjectType.Layout.PROFILE)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenEditor)
        assertEquals("id1", nav.target)
    }

    @Test
    fun `set layout navigates to OpenDataView`() {
        val obj = basicObj(layout = ObjectType.Layout.SET)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenDataView)
    }

    @Test
    fun `collection layout navigates to OpenDataView`() {
        val obj = basicObj(layout = ObjectType.Layout.COLLECTION)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenDataView)
    }

    @Test
    fun `chat_derived layout navigates to OpenChat`() {
        val obj = basicObj(layout = ObjectType.Layout.CHAT_DERIVED)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenChat)
    }

    @Test
    fun `date layout navigates to OpenDateObject`() {
        val obj = basicObj(layout = ObjectType.Layout.DATE)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenDateObject)
    }

    @Test
    fun `participant layout navigates to OpenParticipant`() {
        val obj = basicObj(layout = ObjectType.Layout.PARTICIPANT)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenParticipant)
    }

    @Test
    fun `object_type layout navigates to OpenType`() {
        val obj = basicObj(layout = ObjectType.Layout.OBJECT_TYPE)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.OpenType)
    }

    @Test
    fun `invalid object returns NonValidObject`() {
        val obj = ObjectWrapper.Basic(emptyMap())
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.NonValidObject)
    }

    @Test
    fun `unknown layout returns UnexpectedLayoutError`() {
        val obj = basicObj(layout = null)
        val nav = obj.navigation()
        assertTrue(nav is OpenObjectNavigation.UnexpectedLayoutError)
    }
}