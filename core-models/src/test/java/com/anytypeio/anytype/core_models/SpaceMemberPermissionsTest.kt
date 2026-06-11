package com.anytypeio.anytype.core_models

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class SpaceMemberPermissionsTest {

    @Test
    fun `isOwnerOrAdmin is true only for owner and admin`() {
        assertTrue(SpaceMemberPermissions.OWNER.isOwnerOrAdmin())
        assertTrue(SpaceMemberPermissions.ADMIN.isOwnerOrAdmin())
        assertFalse(SpaceMemberPermissions.WRITER.isOwnerOrAdmin())
        assertFalse(SpaceMemberPermissions.READER.isOwnerOrAdmin())
        assertFalse(SpaceMemberPermissions.NO_PERMISSIONS.isOwnerOrAdmin())
    }

    @Test
    fun `admin code matches middleware participant permission`() {
        // Guards against accidental reordering: ADMIN must stay 4 to match the proto enum.
        assertEquals(4, SpaceMemberPermissions.ADMIN.code)
    }
}
