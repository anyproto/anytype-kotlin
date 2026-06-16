package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.primitives.SpaceId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkResolverExtensionsTest {

    @Test
    fun `DeepLinkToObject yields its space id`() {
        val action = DeepLinkResolver.Action.DeepLinkToObject(
            obj = "obj-1",
            space = SpaceId("space-1")
        )
        assertEquals("space-1", action.preferredSpaceId())
    }

    @Test
    fun `OsWidget DeepLinkToSpace yields its space id`() {
        val action = DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToSpace(
            space = SpaceId("space-2")
        )
        assertEquals("space-2", action.preferredSpaceId())
    }

    @Test
    fun `OsWidget DeepLinkToObject yields its space id`() {
        val action = DeepLinkResolver.Action.OsWidgetDeepLink.DeepLinkToObject(
            obj = "obj-2",
            space = SpaceId("space-3")
        )
        assertEquals("space-3", action.preferredSpaceId())
    }

    @Test
    fun `Invite has no preferred space`() {
        assertNull(DeepLinkResolver.Action.Invite("link").preferredSpaceId())
    }

    @Test
    fun `InitiateOneToOneChat has no preferred space`() {
        val action = DeepLinkResolver.Action.InitiateOneToOneChat(
            identity = "id-1",
            metadataKey = "key-1"
        )
        assertNull(action.preferredSpaceId())
    }
}
