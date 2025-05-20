package com.anytypeio.anytype.presentation.notifications

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface NotificationPermissionManager {
    fun shouldShowPermissionDialog(): Boolean
    fun onPermissionRequested()
    fun onPermissionGranted()
    fun onPermissionDenied()
    fun onPermissionDismissed()
}

class NotificationPermissionManagerImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : NotificationPermissionManager {
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.NotRequested)
    val permissionState: StateFlow<PermissionState> = _permissionState

    override fun shouldShowPermissionDialog(): Boolean {

        // If notifications are already enabled at the system level, no dialog needed
        if (areNotificationsEnabled()) {
            return false
        }

        val lastRequestTime = sharedPreferences.getLong(KEY_LAST_REQUEST_TIME, 0)
        val requestCount = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        val currentTime = System.currentTimeMillis()

        return when {
            // First time request
            lastRequestTime == 0L -> true
            // User clicked "Not now" - show again after 24 hours, max 3 times
            requestCount < MAX_REQUEST_COUNT && (currentTime - lastRequestTime) >= HOURS_24 -> true
            // User rejected in system dialog - show banner in settings
            _permissionState.value == PermissionState.Denied -> false
            else -> false
        }
    }

    override fun onPermissionRequested() {
        val currentCount = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        sharedPreferences.edit().apply {
            putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis())
            putInt(KEY_REQUEST_COUNT, currentCount + 1)
            apply()
        }
    }

    override fun onPermissionGranted() {
        _permissionState.value = PermissionState.Granted
        sharedPreferences.edit().apply {
            putBoolean(KEY_PERMISSION_GRANTED, true)
            apply()
        }
    }

    override fun onPermissionDenied() {
        _permissionState.value = PermissionState.Denied
        sharedPreferences.edit().apply {
            putBoolean(KEY_PERMISSION_GRANTED, false)
            apply()
        }
    }

    override fun onPermissionDismissed() {
        _permissionState.value = PermissionState.Dismissed
    }

    /**
     * Returns true if notifications can be posted:
     *  - on API<33, checks whether the user has globally disabled notifications for your app
     *  - on API>=33, also checks the new POST_NOTIFICATIONS runtime permission
     */
    private fun areNotificationsEnabled(): Boolean {
        // 1) global switch
        val managerCompat = NotificationManagerCompat.from(context)
        if (!managerCompat.areNotificationsEnabled()) {
            return false
        }

        // 2) on Tiramisu+, must also hold POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        }

        return true
    }

    sealed class PermissionState {
        object NotRequested : PermissionState()
        object Granted : PermissionState()
        object Denied : PermissionState()
        object Dismissed : PermissionState()
    }

    companion object {
        private const val KEY_LAST_REQUEST_TIME = "notification_permission_last_request_time"
        private const val KEY_REQUEST_COUNT = "notification_permission_request_count"
        private const val KEY_PERMISSION_GRANTED = "notification_permission_granted"
        private const val MAX_REQUEST_COUNT = 3
        private const val HOURS_24 = 24 * 60 * 60 * 1000L
    }
} 