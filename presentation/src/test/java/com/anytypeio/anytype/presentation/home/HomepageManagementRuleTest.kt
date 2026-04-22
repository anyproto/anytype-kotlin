package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class HomepageManagementRuleTest {

    @Test
    fun `owner in regular space can manage homepage`() {
        assertTrue(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = false,
                permission = SpaceMemberPermissions.OWNER
            )
        )
    }

    @Test
    fun `writer in regular space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = false,
                permission = SpaceMemberPermissions.WRITER
            )
        )
    }

    @Test
    fun `reader in regular space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = false,
                permission = SpaceMemberPermissions.READER
            )
        )
    }

    @Test
    fun `no permission in regular space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = false,
                permission = SpaceMemberPermissions.NO_PERMISSIONS
            )
        )
    }

    @Test
    fun `null permission in regular space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = false,
                permission = null
            )
        )
    }

    @Test
    fun `owner in one-on-one space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = true,
                permission = SpaceMemberPermissions.OWNER
            )
        )
    }

    @Test
    fun `writer in one-on-one space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = true,
                permission = SpaceMemberPermissions.WRITER
            )
        )
    }

    @Test
    fun `null permission in one-on-one space cannot manage homepage`() {
        assertFalse(
            HomepageManagementRule.canManageHomepage(
                isOneToOneSpace = true,
                permission = null
            )
        )
    }

    @Test
    fun `matrix covers all combinations`() {
        val cases = listOf(
            Triple(false, SpaceMemberPermissions.OWNER, true),
            Triple(false, SpaceMemberPermissions.WRITER, false),
            Triple(false, SpaceMemberPermissions.READER, false),
            Triple(false, SpaceMemberPermissions.NO_PERMISSIONS, false),
            Triple(true, SpaceMemberPermissions.OWNER, false),
            Triple(true, SpaceMemberPermissions.WRITER, false),
            Triple(true, SpaceMemberPermissions.READER, false),
            Triple(true, SpaceMemberPermissions.NO_PERMISSIONS, false),
        )
        for ((isOneToOne, permission, expected) in cases) {
            assertEquals(
                expected,
                HomepageManagementRule.canManageHomepage(isOneToOne, permission),
                message = "isOneToOne=$isOneToOne, permission=$permission"
            )
        }
    }
}
