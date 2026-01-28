package com.anytypeio.anytype.presentation.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_utils.notifications.NotificationPermissionManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class NotificationPermissionManagerImplTest {

    private lateinit var sharedPreferences: SharedPreferences
    private val dispatcher = StandardTestDispatcher(name = "Default test dispatcher")
    private val testScope = TestScope(dispatcher)
    private lateinit var manager: NotificationPermissionManagerImpl

    private lateinit var notificationManager: NotificationManager
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setUp() {
        // Use a real SharedPreferences from Robolectric
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

        // Clear shared preferences before each test
        sharedPreferences.edit().clear().apply()

        // Grab NotificationManager and shadow it
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = Shadows.shadowOf(notificationManager)

        // By default, simulate notifications being DISABLED at the system level
        shadowNotificationManager.setNotificationsEnabled(false)

        manager = NotificationPermissionManagerImpl(
            sharedPreferences = sharedPreferences,
            context = context
        )
    }

    @After
    fun tearDown() {
        // Clean up coroutines
        testScope.cancel()
        // Clear shared preferences after each test
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun `should show dialog on first request`() = runTest {
        assertTrue(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog if notifications globally enabled`() = runTest {
        // Simulate user has notifications turned ON in OS settings
        shadowNotificationManager.setNotificationsEnabled(true)

        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog if already shown before`() = runTest {
        // Simulate dialog was shown before
        sharedPreferences.edit()
            .putBoolean(KEY_DIALOG_SHOWN, true)
            .apply()

        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog if dismissed before`() = runTest {
        // Simulate dialog was dismissed before
        sharedPreferences.edit()
            .putBoolean(KEY_DIALOG_DISMISSED, true)
            .apply()

        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog if both shown and dismissed`() = runTest {
        // Simulate dialog was both shown and dismissed
        sharedPreferences.edit()
            .putBoolean(KEY_DIALOG_SHOWN, true)
            .putBoolean(KEY_DIALOG_DISMISSED, true)
            .apply()

        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog when permission is denied`() = runTest {
        // First request permission to set up initial state
        manager.onPermissionRequested()
        // Then deny it
        manager.onPermissionDenied()
        
        assertEquals(NotificationPermissionManagerImpl.PermissionState.Denied, manager.permissionState.first())
        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `onPermissionRequested should set dialog shown flag`() = runTest {
        assertFalse(sharedPreferences.getBoolean(KEY_DIALOG_SHOWN, false))
        
        manager.onPermissionRequested()

        assertTrue(sharedPreferences.getBoolean(KEY_DIALOG_SHOWN, false))
    }

    @Test
    fun `onPermissionGranted should update state and save to preferences`() = runTest {
        manager.onPermissionGranted()

        assertEquals(NotificationPermissionManagerImpl.PermissionState.Granted, manager.permissionState.first())
        assertTrue(sharedPreferences.getBoolean(KEY_PERMISSION_GRANTED, false))
    }

    @Test
    fun `onPermissionDenied should update state and save to preferences`() = runTest {
        manager.onPermissionDenied()

        assertEquals(NotificationPermissionManagerImpl.PermissionState.Denied, manager.permissionState.first())
        assertFalse(sharedPreferences.getBoolean(KEY_PERMISSION_GRANTED, true))
    }

    @Test
    fun `onPermissionDismissed should update state and set dismissed flag`() = runTest {
        assertFalse(sharedPreferences.getBoolean(KEY_DIALOG_DISMISSED, false))
        
        manager.onPermissionDismissed()

        assertEquals(NotificationPermissionManagerImpl.PermissionState.Dismissed, manager.permissionState.first())
        assertTrue(sharedPreferences.getBoolean(KEY_DIALOG_DISMISSED, false))
    }

    @Test
    fun `should not show dialog after permission request followed by dismissal`() = runTest {
        // First time - should show
        assertTrue(manager.shouldShowPermissionDialog())
        
        // User sees dialog and dismisses
        manager.onPermissionRequested()
        manager.onPermissionDismissed()
        
        // Should not show again
        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog after permission request followed by grant`() = runTest {
        // First time - should show
        assertTrue(manager.shouldShowPermissionDialog())
        
        // User sees dialog and grants permission
        manager.onPermissionRequested()
        manager.onPermissionGranted()
        
        // Should not show again
        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog after permission request followed by denial`() = runTest {
        // First time - should show
        assertTrue(manager.shouldShowPermissionDialog())
        
        // User sees dialog and denies permission
        manager.onPermissionRequested()
        manager.onPermissionDenied()
        
        // Should not show again (since it was denied at system level)
        assertFalse(manager.shouldShowPermissionDialog())
    }

    companion object {
        private const val KEY_DIALOG_SHOWN = "notification_permission_dialog_shown"
        private const val KEY_DIALOG_DISMISSED = "notification_permission_dialog_dismissed"
        private const val KEY_PERMISSION_GRANTED = "notification_permission_granted"
    }
} 