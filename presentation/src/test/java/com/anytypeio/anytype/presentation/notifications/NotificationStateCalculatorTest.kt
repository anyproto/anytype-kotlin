package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
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
} 