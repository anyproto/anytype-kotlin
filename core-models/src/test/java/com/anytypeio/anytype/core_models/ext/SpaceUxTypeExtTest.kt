package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpaceUxTypeExtTest {

    // shouldNavigateDirectlyToChat tests

    @Test
    fun `CHAT space should not navigate directly to chat`() {
        assertFalse(SpaceUxType.CHAT.shouldNavigateDirectlyToChat)
    }

    @Test
    fun `ONE_TO_ONE space should navigate directly to chat`() {
        assertTrue(SpaceUxType.ONE_TO_ONE.shouldNavigateDirectlyToChat)
    }

    @Test
    fun `DATA space should not navigate directly to chat`() {
        assertFalse(SpaceUxType.DATA.shouldNavigateDirectlyToChat)
    }

    @Test
    fun `STREAM space should not navigate directly to chat`() {
        assertFalse(SpaceUxType.STREAM.shouldNavigateDirectlyToChat)
    }

    @Test
    fun `NONE space should not navigate directly to chat`() {
        assertFalse(SpaceUxType.NONE.shouldNavigateDirectlyToChat)
    }

    @Test
    fun `null space type should not navigate directly to chat`() {
        val nullType: SpaceUxType? = null
        assertFalse(nullType.shouldNavigateDirectlyToChat)
    }

    // shouldShowMemberCount tests

    @Test
    fun `CHAT space should show member count`() {
        assertTrue(SpaceUxType.CHAT.shouldShowMemberCount)
    }

    @Test
    fun `ONE_TO_ONE space should not show member count`() {
        assertFalse(SpaceUxType.ONE_TO_ONE.shouldShowMemberCount)
    }

    @Test
    fun `DATA space should show member count`() {
        assertTrue(SpaceUxType.DATA.shouldShowMemberCount)
    }

    @Test
    fun `STREAM space should show member count`() {
        assertTrue(SpaceUxType.STREAM.shouldShowMemberCount)
    }

    @Test
    fun `NONE space should show member count`() {
        assertTrue(SpaceUxType.NONE.shouldShowMemberCount)
    }

    @Test
    fun `null space type should show member count`() {
        val nullType: SpaceUxType? = null
        assertTrue(nullType.shouldShowMemberCount)
    }

    // canCreateAdditionalChats tests

    @Test
    fun `CHAT space should not allow creating additional chats`() {
        assertFalse(SpaceUxType.CHAT.canCreateAdditionalChats)
    }

    @Test
    fun `ONE_TO_ONE space should not allow creating additional chats`() {
        assertFalse(SpaceUxType.ONE_TO_ONE.canCreateAdditionalChats)
    }

    @Test
    fun `DATA space should allow creating additional chats`() {
        assertTrue(SpaceUxType.DATA.canCreateAdditionalChats)
    }

    @Test
    fun `STREAM space should allow creating additional chats`() {
        assertTrue(SpaceUxType.STREAM.canCreateAdditionalChats)
    }

    @Test
    fun `NONE space should allow creating additional chats`() {
        assertTrue(SpaceUxType.NONE.canCreateAdditionalChats)
    }

    @Test
    fun `null space type should allow creating additional chats`() {
        val nullType: SpaceUxType? = null
        assertTrue(nullType.canCreateAdditionalChats)
    }

    // region ObjectWrapper.SpaceView extensions
    //
    // These tests cover the SpaceView-receiver helpers, which route through
    // isOneToOneSpace / isChatSpace and therefore respect the new spaceType
    // relation with the legacy spaceUxType fallback.

    private fun spaceViewWith(
        spaceType: SpaceType? = null,
        spaceUxType: SpaceUxType? = null
    ) = ObjectWrapper.SpaceView(
        map = buildMap<String, Any?> {
            if (spaceType != null) put(Relations.SPACE_TYPE, spaceType.code.toDouble())
            if (spaceUxType != null) put(Relations.SPACE_UX_TYPE, spaceUxType.code.toDouble())
        }
    )

    @Test
    fun `SpaceView shouldNavigateDirectlyToChat reads spaceType ONE_TO_ONE`() {
        assertTrue(spaceViewWith(spaceType = SpaceType.ONE_TO_ONE).shouldNavigateDirectlyToChat)
    }

    @Test
    fun `SpaceView shouldNavigateDirectlyToChat falls back to spaceUxType ONE_TO_ONE`() {
        assertTrue(spaceViewWith(spaceUxType = SpaceUxType.ONE_TO_ONE).shouldNavigateDirectlyToChat)
    }

    @Test
    fun `SpaceView shouldNavigateDirectlyToChat is false for REGULAR even with legacy ONE_TO_ONE`() {
        assertFalse(
            spaceViewWith(
                spaceType = SpaceType.REGULAR,
                spaceUxType = SpaceUxType.ONE_TO_ONE
            ).shouldNavigateDirectlyToChat
        )
    }

    @Test
    fun `SpaceView shouldShowMemberCount is false for ONE_TO_ONE`() {
        assertFalse(spaceViewWith(spaceType = SpaceType.ONE_TO_ONE).shouldShowMemberCount)
    }

    @Test
    fun `SpaceView shouldShowMemberCount is true for REGULAR`() {
        assertTrue(spaceViewWith(spaceType = SpaceType.REGULAR).shouldShowMemberCount)
    }

    @Test
    fun `SpaceView shouldShowMemberCount falls back to legacy when spaceType absent`() {
        assertFalse(spaceViewWith(spaceUxType = SpaceUxType.ONE_TO_ONE).shouldShowMemberCount)
        assertTrue(spaceViewWith(spaceUxType = SpaceUxType.DATA).shouldShowMemberCount)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats is false for CHAT`() {
        assertFalse(spaceViewWith(spaceType = SpaceType.CHAT).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats is false for ONE_TO_ONE`() {
        assertFalse(spaceViewWith(spaceType = SpaceType.ONE_TO_ONE).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats is true for REGULAR`() {
        assertTrue(spaceViewWith(spaceType = SpaceType.REGULAR).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats falls back to legacy when spaceType absent`() {
        assertFalse(spaceViewWith(spaceUxType = SpaceUxType.CHAT).canCreateAdditionalChats)
        assertFalse(spaceViewWith(spaceUxType = SpaceUxType.ONE_TO_ONE).canCreateAdditionalChats)
        assertTrue(spaceViewWith(spaceUxType = SpaceUxType.DATA).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats prefers spaceType over legacy CHAT`() {
        assertTrue(
            spaceViewWith(
                spaceType = SpaceType.REGULAR,
                spaceUxType = SpaceUxType.CHAT
            ).canCreateAdditionalChats
        )
    }

    // endregion
}
