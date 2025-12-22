package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpaceUxTypeExtTest {

    // shouldNavigateDirectlyToChat tests

    @Test
    fun `CHAT space should navigate directly to chat`() {
        assertTrue(SpaceUxType.CHAT.shouldNavigateDirectlyToChat)
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

    // shouldShowMessageAuthorInPreview tests

    @Test
    fun `CHAT space should show message author in preview`() {
        assertTrue(SpaceUxType.CHAT.shouldShowMessageAuthorInPreview)
    }

    @Test
    fun `ONE_TO_ONE space should not show message author in preview`() {
        assertFalse(SpaceUxType.ONE_TO_ONE.shouldShowMessageAuthorInPreview)
    }

    @Test
    fun `DATA space should show message author in preview`() {
        assertTrue(SpaceUxType.DATA.shouldShowMessageAuthorInPreview)
    }

    @Test
    fun `STREAM space should show message author in preview`() {
        assertTrue(SpaceUxType.STREAM.shouldShowMessageAuthorInPreview)
    }

    @Test
    fun `NONE space should show message author in preview`() {
        assertTrue(SpaceUxType.NONE.shouldShowMessageAuthorInPreview)
    }

    @Test
    fun `null space type should show message author in preview`() {
        val nullType: SpaceUxType? = null
        assertTrue(nullType.shouldShowMessageAuthorInPreview)
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
}
