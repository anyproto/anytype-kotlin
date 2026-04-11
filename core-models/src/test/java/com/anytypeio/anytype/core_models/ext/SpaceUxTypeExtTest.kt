package com.anytypeio.anytype.core_models.ext

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.multiplayer.SpaceType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpaceUxTypeExtTest {

    // These tests cover the SpaceView-receiver helpers, which route through
    // isOneToOneSpace and therefore respect the new spaceType relation
    // with the legacy spaceUxType fallback.

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
    fun `SpaceView canCreateAdditionalChats is false for ONE_TO_ONE`() {
        assertFalse(spaceViewWith(spaceType = SpaceType.ONE_TO_ONE).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats is true for REGULAR`() {
        assertTrue(spaceViewWith(spaceType = SpaceType.REGULAR).canCreateAdditionalChats)
    }

    @Test
    fun `SpaceView canCreateAdditionalChats falls back to legacy when spaceType absent`() {
        assertFalse(spaceViewWith(spaceUxType = SpaceUxType.ONE_TO_ONE).canCreateAdditionalChats)
        assertTrue(spaceViewWith(spaceUxType = SpaceUxType.DATA).canCreateAdditionalChats)
    }
}
