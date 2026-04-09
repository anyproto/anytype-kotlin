package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for DROID-4464: SpaceType migration.
 *
 * Verifies the new `spaceType` getter on ObjectWrapper.SpaceView and that
 * `isOneToOneSpace` prefers `spaceType` over the legacy `spaceUxType`, with
 * a back-compat fallback for records that don't yet carry the new relation.
 */
class ObjectWrapperSpaceTypeTest {

    @Test
    fun `spaceType returns null when relation is absent`() {
        val view = ObjectWrapper.SpaceView(map = emptyMap<String, Any?>())
        assertNull(view.spaceType)
    }

    @Test
    fun `spaceType returns null when relation is explicitly null`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to null)
        )
        assertNull(view.spaceType)
    }

    @Test
    fun `spaceType returns UNKNOWN for code 0`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to 0.0)
        )
        assertEquals(SpaceType.UNKNOWN, view.spaceType)
    }

    @Test
    fun `spaceType returns REGULAR for code 1`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to 1.0)
        )
        assertEquals(SpaceType.REGULAR, view.spaceType)
    }

    @Test
    fun `spaceType returns TECH for code 2`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to 2.0)
        )
        assertEquals(SpaceType.TECH, view.spaceType)
    }

    @Test
    fun `spaceType returns CHAT for code 3`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to 3.0)
        )
        assertEquals(SpaceType.CHAT, view.spaceType)
    }

    @Test
    fun `spaceType returns ONE_TO_ONE for code 4`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(Relations.SPACE_TYPE to 4.0)
        )
        assertEquals(SpaceType.ONE_TO_ONE, view.spaceType)
    }

    @Test
    fun `isOneToOneSpace is true when spaceType is ONE_TO_ONE`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.ONE_TO_ONE.code.toDouble()
            )
        )
        assertTrue(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace falls back to spaceUxType when spaceType is null`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_UX_TYPE to SpaceUxType.ONE_TO_ONE.code.toDouble()
            )
        )
        assertNull(view.spaceType)
        assertTrue(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace falls back to spaceUxType when spaceType is UNKNOWN`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.UNKNOWN.code.toDouble(),
                Relations.SPACE_UX_TYPE to SpaceUxType.ONE_TO_ONE.code.toDouble()
            )
        )
        assertEquals(SpaceType.UNKNOWN, view.spaceType)
        assertTrue(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace is false when spaceType UNKNOWN and spaceUxType not ONE_TO_ONE`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.UNKNOWN.code.toDouble(),
                Relations.SPACE_UX_TYPE to SpaceUxType.DATA.code.toDouble()
            )
        )
        assertFalse(view.isOneToOneSpace)
    }

    @Test
    fun `spaceType wins over spaceUxType when both set and disagree`() {
        // Middleware is the source of truth: if spaceType says REGULAR but
        // legacy spaceUxType still says ONE_TO_ONE (stale derivation),
        // the new field takes precedence.
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.REGULAR.code.toDouble(),
                Relations.SPACE_UX_TYPE to SpaceUxType.ONE_TO_ONE.code.toDouble()
            )
        )
        assertFalse(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace is false for REGULAR space`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.REGULAR.code.toDouble()
            )
        )
        assertFalse(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace is false for TECH space`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.TECH.code.toDouble()
            )
        )
        assertFalse(view.isOneToOneSpace)
    }

    @Test
    fun `isOneToOneSpace is false for CHAT space`() {
        val view = ObjectWrapper.SpaceView(
            map = mapOf<String, Any?>(
                Relations.SPACE_TYPE to SpaceType.CHAT.code.toDouble()
            )
        )
        assertFalse(view.isOneToOneSpace)
    }

}
