package com.anytypeio.anytype.presentation.notifications

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl.PermissionState
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

interface NotificationPermissionManager {
    fun shouldShowPermissionDialog(): Boolean
    fun onPermissionRequested()
    fun onPermissionGranted()
    fun onPermissionDenied()
    fun onPermissionDismissed()
    fun permissionState(): Flow<PermissionState>
    fun refreshPermissionState()
    fun areNotificationsEnabled(): Boolean
}

class NotificationPermissionManagerImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : NotificationPermissionManager {
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.NotRequested)
    val permissionState: StateFlow<PermissionState> = _permissionState

    override fun permissionState(): StateFlow<PermissionState> {
        return _permissionState
    }

    override fun shouldShowPermissionDialog(): Boolean {
        // If notifications are already enabled at the system level, no dialog needed
        if (areNotificationsEnabled()) {
            return false
        }

        // Check if user has ever been shown the dialog before
        val hasBeenShown = sharedPreferences.getBoolean(KEY_DIALOG_SHOWN, false)
        
        // Check if user has dismissed the dialog before
        val hasBeenDismissed = sharedPreferences.getBoolean(KEY_DIALOG_DISMISSED, false)

        return when {
            // Show only if never shown before and never dismissed
            !hasBeenShown && !hasBeenDismissed -> true
            // User rejected in system dialog - show banner in settings
            _permissionState.value == PermissionState.Denied -> false
            else -> false
        }
    }

    override fun onPermissionRequested() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_DIALOG_SHOWN, true)
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
        sharedPreferences.edit().apply {
            putBoolean(KEY_DIALOG_DISMISSED, true)
            apply()
        }
    }

    /**
     * Returns true if notifications can be posted:
     *  - on API<33, checks whether the user has globally disabled notifications for your app
     *  - on API>=33, also checks the new POST_NOTIFICATIONS runtime permission
     */
    override fun areNotificationsEnabled(): Boolean {
        return try {
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

            true
        } catch (e: Exception) {
            Timber.w(e, "Error checking notification permissions")
            // If we can't determine the state safely, assume notifications are disabled
            // This prevents crashes when the app is in an unstable state
            false
        }
    }

    override fun refreshPermissionState() {
        try {
            _permissionState.value = when {
                areNotificationsEnabled() -> PermissionState.Granted
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        PermissionState.Granted
                    } else {
                        PermissionState.Denied
                    }
                }

                else -> PermissionState.Denied
            }
        } catch (e: Exception) {
            Timber.w(e, "Error refreshing notification permission state")
            _permissionState.value = PermissionState.Denied
        }
    }

    sealed class PermissionState {
        object NotRequested : PermissionState()
        object Granted : PermissionState()
        object Denied : PermissionState()
        object Dismissed : PermissionState()
    }

    companion object {
        private const val KEY_DIALOG_SHOWN = "notification_permission_dialog_shown"
        private const val KEY_DIALOG_DISMISSED = "notification_permission_dialog_dismissed"
        private const val KEY_PERMISSION_GRANTED = "notification_permission_granted"
    }
} 