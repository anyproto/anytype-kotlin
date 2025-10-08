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
    private lateinit var mockNotificationPermissionManager: NotificationPermissionManager

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
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(spaceView, mockNotificationPermissionManager)

        // Then
        assertFalse("Should return false when spaceView is null", result)
    }

    @Test
    fun `calculateMutedState should return false when space is unmuted and app notifications enabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertFalse("Should return false when space allows all notifications and app notifications are enabled", result)
    }

    @Test
    fun `calculateMutedState should return true when space is disabled and app notifications enabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when space notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true when space is mentions only and app notifications enabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when space is set to mentions only", result)
    }

    @Test
    fun `calculateMutedState should return true when space is unmuted but app notifications disabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when app notifications are disabled globally", result)
    }

    @Test
    fun `calculateMutedState should return true when space is disabled and app notifications disabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when both space and app notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true when space is mentions only and app notifications disabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when space is mentions only and app notifications are disabled", result)
    }

    // Edge case tests
    @Test
    fun `calculateMutedState should handle null spacePushNotificationMode gracefully`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(null)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertFalse("Should return false when spacePushNotificationMode is null and app notifications enabled", result)
    }

    @Test
    fun `calculateMutedState should return true when spacePushNotificationMode is null and app notifications disabled`() {
        // Given
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(null)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Should return true when app notifications are disabled, regardless of space settings", result)
    }

    // Chat space specific tests
    @Test
    fun `calculateMutedState should return false for chat space when space is unmuted even if app notifications disabled`() {
        // Given - Chat space with space-level notifications enabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertFalse("Chat space should show unmuted state (false) when space notifications are enabled, regardless of app notification state", result)
    }

    @Test
    fun `calculateMutedState should return true for chat space when space is muted even if app notifications enabled`() {
        // Given - Chat space with space-level notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.DISABLE)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Chat space should show muted state (true) when space notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should return true for chat space when space is mentions only`() {
        // Given - Chat space with mentions-only notifications
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.CHAT)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.MENTIONS)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(true)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Chat space should show muted state (true) when space is set to mentions only", result)
    }

    @Test
    fun `calculateMutedState should return true for data space when app notifications disabled even if space unmuted`() {
        // Given - Data space with space-level notifications enabled but app notifications disabled
        `when`(mockSpaceView.spaceUxType).thenReturn(SpaceUxType.DATA)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Data space should show muted state (true) when app notifications are disabled", result)
    }

    @Test
    fun `calculateMutedState should handle null spaceUxType as non-chat space`() {
        // Given - Space with null spaceUxType (should be treated as non-chat space)
        `when`(mockSpaceView.spaceUxType).thenReturn(null)
        `when`(mockSpaceView.spacePushNotificationMode).thenReturn(NotificationState.ALL)
        `when`(mockNotificationPermissionManager.areNotificationsEnabled()).thenReturn(false)

        // When
        val result = NotificationStateCalculator.calculateMutedState(mockSpaceView, mockNotificationPermissionManager)

        // Then
        assertTrue("Space with null spaceUxType should be treated as non-chat and show muted when app notifications disabled", result)
    }
} 