package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class NotificationStateCalculatorTest {

    @Mock
    private lateinit var mockSpaceView: ObjectWrapper.SpaceView

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `calculateMutedState should return false when spaceView is null`() {
        // Given
        val spaceView: ObjectWrapper.SpaceView? = null

        // When
        val result = NotificationStateCalculator.calculateMutedState(spaceView)

        // Then
        assertFalse("Should return false when spaceView is null", result)
    }

    @Test
    fun `calculateMutedState should return false when space is unmuted`() {
        // Given - Space with chat functionality and notifications enabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertFalse("Should return false when space allows all notifications", result)
    }

    @Test
    fun `calculateMutedState should return true when space is disabled`() {
        // Given - Space with chat functionality but notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Should return true when space notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true when space is mentions only`() {
        // Given - Space with chat functionality set to mentions only
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Should return true when space is set to mentions only", result)
    }

    @Test
    fun `calculateMutedState should ignore app notifications state for chat spaces`() {
        // Given - Space with chat and space notifications enabled, but app notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertFalse("Should return false - app notification state is ignored for chat spaces", result)
    }

    @Test
    fun `calculateMutedState should return true when space disabled regardless of app notifications`() {
        // Given - Space with chat and space notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Should return true when space notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true when mentions only regardless of app notifications`() {
        // Given - Space with chat and mentions-only mode
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Should return true when space is mentions only", result)
    }

    // Edge case tests
    @Test
    fun `calculateMutedState should handle null spacePushNotificationMode gracefully`() {
        // Given - Space with chat but null notification mode
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(null)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertFalse("Should return false when spacePushNotificationMode is null (treated as unmuted)", result)
    }

    @Test
    fun `calculateMutedState should return false when called for space without chat`() {
        // Given - Space WITHOUT chat functionality
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.DATA)
        `when`(mockSpaceView.chatId).thenReturn(null)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then - Should return false (unmuted) as safe default
        assertFalse("Should return false (unmuted) for non-chat spaces as safe default", result)
    }

    // Chat space specific tests
    @Test
    fun `calculateMutedState should return false for chat space when space is unmuted even if app notifications disabled`() {
        // Given - Chat space with space-level notifications enabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertFalse("Chat space should show unmuted state (false) when space notifications are enabled, regardless of app notification state", result)
    }

    @Test
    fun `calculateMutedState should return true for chat space when space is muted even if app notifications enabled`() {
        // Given - Chat space with space-level notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Chat space should show muted state (true) when space notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true for chat space when space is mentions only`() {
        // Given - Chat space with mentions-only notifications
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView)

        // Then
        assertTrue("Chat space should show muted state (true) when space is set to mentions only", result)
    }

    // ========== calculateChatNotificationState Tests ==========

    // Force list priority tests
    @Test
    fun `calculateChatNotificationState should return ALL when chat is in forceAllIds list`() {
        // Given - Space default is DISABLE, but chat is in forceAllIds
        val chatId = "chat1"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(listOf(chatId))
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return ALL when chat is in forceAllIds list, overriding space default", NotificationState.ALL, result)
    }

    @Test
    fun `calculateChatNotificationState should return MENTIONS when chat is in forceMentionIds list`() {
        // Given - Space default is ALL, but chat is in forceMentionIds
        val chatId = "chat2"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(listOf(chatId))
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return MENTIONS when chat is in forceMentionIds list, overriding space default", NotificationState.MENTIONS, result)
    }

    @Test
    fun `calculateChatNotificationState should return DISABLE when chat is in forceMuteIds list`() {
        // Given - Space default is ALL, but chat is in forceMuteIds
        val chatId = "chat3"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(listOf(chatId))

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return DISABLE when chat is in forceMuteIds list, overriding space default", NotificationState.DISABLE, result)
    }

    // Space default fallback tests
    @Test
    fun `calculateChatNotificationState should return space default ALL when chat not in force lists`() {
        // Given - Space default is ALL and chat is not in any force list
        val chatId = "chat4"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return space default ALL when chat is not in any force list", NotificationState.ALL, result)
    }

    @Test
    fun `calculateChatNotificationState should return space default MENTIONS when chat not in force lists`() {
        // Given - Space default is MENTIONS and chat is not in any force list
        val chatId = "chat5"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return space default MENTIONS when chat is not in any force list", NotificationState.MENTIONS, result)
    }

    @Test
    fun `calculateChatNotificationState should return space default DISABLE when chat not in force lists`() {
        // Given - Space default is DISABLE and chat is not in any force list
        val chatId = "chat6"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should return space default DISABLE when chat is not in any force list", NotificationState.DISABLE, result)
    }

    // Edge case tests
    @Test
    fun `calculateChatNotificationState should handle empty force lists`() {
        // Given - All force lists are empty, space default is ALL
        val chatId = "chat7"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Should fall back to space default when all force lists are empty", NotificationState.ALL, result)
    }

    @Test
    fun `calculateChatNotificationState should handle multiple chats with different settings`() {
        // Given - Multiple chats with different force list settings
        val chat1 = "chat1"
        val chat2 = "chat2"
        val chat3 = "chat3"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(listOf(chat1))
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(listOf(chat2))

        // When - Check each chat
        val result1 = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chat1)
        val result2 = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chat2)
        val result3 = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chat3)

        // Then - Each chat has correct state
        assertEquals("Chat1 should be ALL (in forceAllIds)", NotificationState.ALL, result1)
        assertEquals("Chat2 should be DISABLE (in forceMuteIds)", NotificationState.DISABLE, result2)
        assertEquals("Chat3 should be MENTIONS (space default, not in any force list)", NotificationState.MENTIONS, result3)
    }

    @Test
    fun `calculateChatNotificationState should prioritize force lists over space default`() {
        // Given - Space default is DISABLE, but chat is in forceAllIds (opposite)
        val chatId = "chat8"
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockSpaceView.spacePushNotificationForceAllIds).thenReturn(listOf(chatId))
        `when`(mockSpaceView.spacePushNotificationForceMentionIds).thenReturn(emptyList())
        `when`(mockSpaceView.spacePushNotificationForceMuteIds).thenReturn(emptyList())

        // When
        val result = NotificationStateCalculator.calculateChatNotificationState(mockSpaceView, chatId)

        // Then
        assertEquals("Force list should take priority over space default", NotificationState.ALL, result)
    }

} 