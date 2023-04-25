package com.anytypeio.anytype.ui_settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.base.BaseUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AboutAppViewModel(
    private val getAccount: GetAccount,
    private val getLibraryVersion: GetLibraryVersion,
    private val analytics: Analytics
) : ViewModel() {

    val navigation = MutableSharedFlow<Navigation>()
    fun onExternalLinkClicked(link: ExternalLink) {
        proceedWithAnalytics(link)
        viewModelScope.launch {
            navigation.emit(Navigation.OpenExternalLink(link))
        }
    }

    private fun proceedWithAnalytics(link: ExternalLink) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = link.eventName,
            props = Props(
                mapOf(
                    "type" to link.eventType
                )
            )
        )
    }

    val libraryVersion = MutableStateFlow("")
    val userId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            getAccount(BaseUseCase.None).process(
                failure = {},
                success = { acc ->
                    userId.value = acc.id
                }
            )
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

    sealed class ExternalLink(val eventName: String, val eventType: String) {
        object WhatIsNew : ExternalLink(
            EVENT_HELP_AND_COMMUNITY,
            EVENT_VALUE_WHAT_IS_NEW
        )

        object AnytypeCommunity : ExternalLink(
            EVENT_HELP_AND_COMMUNITY,
            EVENT_VALUE_COMMUNITY
        )

        object HelpAndTutorials : ExternalLink(
            EVENT_HELP_AND_COMMUNITY,
            EVENT_VALUE_HELP_AND_TUTORIALS
        )

        object TermsOfUse : ExternalLink(
            EVENT_LEGAL,
            EVENT_VALUE_TERMS_OF_USE
        )

        object PrivacyPolicy : ExternalLink(
            EVENT_LEGAL,
            EVENT_VALUE_PRIVACY_POLICY
        )
    }

    class Factory(
        private val getAccount: GetAccount,
        private val getLibraryVersion: GetLibraryVersion,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AboutAppViewModel(
                getAccount = getAccount,
                getLibraryVersion = getLibraryVersion,
                analytics = analytics
            ) as T
        }
    }
}

private const val EVENT_HELP_AND_COMMUNITY = "Help_and_Community"
private const val EVENT_LEGAL = "Legal"
private const val EVENT_VALUE_TERMS_OF_USE = "terms_of_use"
private const val EVENT_VALUE_PRIVACY_POLICY = "privacy_policy"
private const val EVENT_VALUE_WHAT_IS_NEW = "what_is_new"
private const val EVENT_VALUE_COMMUNITY = "anytype_community"
private const val EVENT_VALUE_HELP_AND_TUTORIALS = "help_and_tutorials"