package com.anytypeio.anytype.presentation.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
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
    fun `should not show dialog if max requests reached`() = runTest {
        // Set up max requests
        repeat(NotificationPermissionManagerImpl.MAX_REQUEST_COUNT) {
            manager.onPermissionRequested()
        }

        assertFalse(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should show dialog after 24 hours if request count less than max`() = runTest {
        // Set up initial request
        manager.onPermissionRequested()

        // Simulate time passing
        val pastTime = System.currentTimeMillis() - HOURS_24 - 1000 // 24 hours + 1 second ago
        sharedPreferences.edit()
            .putLong(KEY_LAST_REQUEST_TIME, pastTime)
            .apply()

        assertTrue(manager.shouldShowPermissionDialog())
    }

    @Test
    fun `should not show dialog before 24 hours have passed`() = runTest {
        // Set up initial request
        manager.onPermissionRequested()

        // Simulate time passing
        val recentTime = System.currentTimeMillis() - HOURS_24 + 1000 // 24 hours - 1 second ago
        sharedPreferences.edit()
            .putLong(KEY_LAST_REQUEST_TIME, recentTime)
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
    fun `onPermissionRequested should increment request count and update timestamp`() = runTest {
        val initialCount = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        manager.onPermissionRequested()

        val newCount = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        assertEquals(initialCount + 1, newCount)
        assertTrue(sharedPreferences.contains(KEY_LAST_REQUEST_TIME))
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
    fun `onPermissionDismissed should update state`() = runTest {
        manager.onPermissionDismissed()

        assertEquals(NotificationPermissionManagerImpl.PermissionState.Dismissed, manager.permissionState.first())
    }

    companion object {
        private const val KEY_LAST_REQUEST_TIME = "notification_permission_last_request_time"
        private const val KEY_REQUEST_COUNT = "notification_permission_request_count"
        private const val KEY_PERMISSION_GRANTED = "notification_permission_granted"
        private const val HOURS_24 = 24 * 60 * 60 * 1000L
    }
} 