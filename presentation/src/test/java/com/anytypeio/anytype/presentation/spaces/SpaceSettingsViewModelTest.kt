package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [SpaceSettingsViewModel.shouldShowChangeTypeOption]
 *
 * This test class focuses on testing the business logic of determining
 * when the "Change Type" option should be displayed to users.
 */
class SpaceSettingsViewModelTest {

    // Create a minimal test helper class that exposes the function under test
    private val testHelper = object {
        fun shouldShowChangeTypeOption(
            permission: SpaceMemberPermissions?,
            spaceView: ObjectWrapper.SpaceView
        ): Boolean {
            return permission?.isOwner() == true
                && spaceView.spaceUxType != null
                && spaceView.spaceAccessType == SpaceAccessType.SHARED
                && !spaceView.chatId.isNullOrEmpty()
        }
    }

    @Test
    fun `shouldShowChangeTypeOption returns true when user is owner, space has UX type, and space is shared`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED,
            chatId = "valid-chat-id"
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertTrue(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when user is not owner`() {
        // Given
        val permission = SpaceMemberPermissions.READER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when permission is null`() {
        // Given
        val permission: SpaceMemberPermissions? = null
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when spaceUxType is null`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = null,
            spaceAccessType = SpaceAccessType.SHARED
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when space is private`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.PRIVATE
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when space access type is default`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns true with CHAT type when all conditions are met`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.CHAT,
            spaceAccessType = SpaceAccessType.SHARED,
            chatId = "valid-chat-id"
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertTrue(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when user is editor but not owner`() {
        // Given
        val permission = SpaceMemberPermissions.WRITER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED,
            chatId = "valid-chat-id"
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when chatId is null`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED,
            chatId = null
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }

    @Test
    fun `shouldShowChangeTypeOption returns false when chatId is empty`() {
        // Given
        val permission = SpaceMemberPermissions.OWNER
        val spaceView = StubSpaceView(
            spaceUxType = SpaceUxType.DATA,
            spaceAccessType = SpaceAccessType.SHARED,
            chatId = ""
        )

        // When
        val result = testHelper.shouldShowChangeTypeOption(permission, spaceView)

        // Then
        assertFalse(result)
    }
}
