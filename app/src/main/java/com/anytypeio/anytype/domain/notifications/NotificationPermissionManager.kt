package com.anytypeio.anytype.domain.notifications

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

@Singleton
class NotificationPermissionManager @Inject constructor(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.NotRequested)
    val permissionState: StateFlow<PermissionState> = _permissionState

    fun shouldShowPermissionDialog(): Boolean {
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

    fun onPermissionRequested() {
        val currentCount = sharedPreferences.getInt(KEY_REQUEST_COUNT, 0)
        sharedPreferences.edit().apply {
            putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis())
            putInt(KEY_REQUEST_COUNT, currentCount + 1)
            apply()
        }
    }

    fun onPermissionGranted() {
        _permissionState.value = PermissionState.Granted
        sharedPreferences.edit().apply {
            putBoolean(KEY_PERMISSION_GRANTED, true)
            apply()
        }
    }

    fun onPermissionDenied() {
        _permissionState.value = PermissionState.Denied
        sharedPreferences.edit().apply {
            putBoolean(KEY_PERMISSION_GRANTED, false)
            apply()
        }
    }

    fun onPermissionDismissed() {
        _permissionState.value = PermissionState.Dismissed
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