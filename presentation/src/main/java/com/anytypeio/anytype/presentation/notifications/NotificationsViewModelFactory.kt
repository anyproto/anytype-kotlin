package com.anytypeio.anytype.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import javax.inject.Inject

class NotificationsViewModelFactory @Inject constructor(
    private val analytics: Analytics,
    private val notificationsProvider: NotificationsProvider
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(
            analytics = analytics,
            notificationsProvider = notificationsProvider
        ) as T
    }
}