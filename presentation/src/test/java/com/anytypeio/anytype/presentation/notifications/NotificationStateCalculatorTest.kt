package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
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

} 