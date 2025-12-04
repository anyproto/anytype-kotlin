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
}
