package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent

class MainSettingsViewModel(
    private val analytics: Analytics
) : ViewModel() {

    fun onAboutClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.aboutScreenShow
        )
    }

    fun onAccountAndDataClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.accountDataSettingsShow
        )
    }

    fun onPersonalizationClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.personalisationSettingsShow
        )
    }

    fun onAppearanceClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.appearanceScreenShow
        )
    }

    class Factory(
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = MainSettingsViewModel(
            analytics = analytics
        ) as T
    }
}