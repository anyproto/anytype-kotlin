package com.anytypeio.anytype.ui_settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AboutAppViewModel(
    private val getAccount: GetAccount,
    private val getLibraryVersion: GetLibraryVersion,
    private val analytics: Analytics,
    private val configStorage: ConfigStorage
) : ViewModel() {

    val navigation = MutableSharedFlow<Navigation>()
    fun onExternalLinkClicked(link: ExternalLink) {
        proceedWithAnalytics(link)
        viewModelScope.launch {
            navigation.emit(Navigation.OpenExternalLink(link))
        }
    }

    fun onContactUsClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.MENU_HELP_CONTACT_US
        )
    }

    private fun proceedWithAnalytics(link: ExternalLink) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = link.eventName
        )
    }

    val libraryVersion = MutableStateFlow("")
    val accountId = MutableStateFlow("")
    val analyticsId = MutableStateFlow("")
    val deviceId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            getAccount.stream(Unit).collect { result ->
                result.fold(
                    onSuccess = { accountId.value = it.id },
                    onFailure = { Timber.e(it, "getAccount error") }
                )
            }
        }
        viewModelScope.launch {
            val config = configStorage.get()
            analyticsId.value = config.analytics
            deviceId.value = config.device
        }
        viewModelScope.launch {
            getLibraryVersion(BaseUseCase.None).process(
                failure = {},
                success = { version ->
                    libraryVersion.value = version
                }
            )
        }
    }

    sealed interface Navigation {
        class OpenExternalLink(val link: ExternalLink) : Navigation
    }

    sealed class ExternalLink(val eventName: String) {
        object WhatIsNew : ExternalLink(EventsDictionary.MENU_HELP_WHAT_IS_NEW)
        object AnytypeCommunity : ExternalLink(EventsDictionary.MENU_HELP_COMMUNITY)
        object HelpAndTutorials : ExternalLink(EventsDictionary.MENU_HELP_TUTORIAL)
        object TermsOfUse : ExternalLink(EventsDictionary.MENU_HELP_TERMS)
        object PrivacyPolicy : ExternalLink(EventsDictionary.MENU_HELP_PRIVACY)
    }

    class Factory @Inject constructor(
        private val getAccount: GetAccount,
        private val getLibraryVersion: GetLibraryVersion,
        private val analytics: Analytics,
        private val configStorage: ConfigStorage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AboutAppViewModel(
                getAccount = getAccount,
                getLibraryVersion = getLibraryVersion,
                analytics = analytics,
                configStorage = configStorage
            ) as T
        }
    }
}