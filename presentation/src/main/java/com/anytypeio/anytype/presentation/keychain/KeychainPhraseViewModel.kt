package com.anytypeio.anytype.presentation.keychain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.keychainCopy
import com.anytypeio.anytype.analytics.base.EventsDictionary.keychainPhraseScreenShow
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import timber.log.Timber

class KeychainPhraseViewModel(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics
) : ViewStateViewModel<ViewState<String>>() {

    init {
        proceedWithGettingMnemonic()
    }

    fun sendShowEvent(type: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = keychainPhraseScreenShow,
            props = Props(
                mapOf(EventsPropertiesKey.type to type)
            )
        )
    }

    fun onCopyClickedFromScreenSettings() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = keychainCopy,
            props = Props(
                mapOf(EventsPropertiesKey.type to EventsDictionary.Type.screenSettings)
            )
        )
    }

    fun onCopyClickedFromLogout() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = keychainCopy,
            props = Props(
                mapOf(EventsPropertiesKey.type to EventsDictionary.Type.beforeLogout)
            )
        )
    }

    private fun proceedWithGettingMnemonic() {
        getMnemonic.invoke(viewModelScope, Unit) { result ->
            result.either(
                fnL = { e -> Timber.e(e, "Error while getting mnemonic") },
                fnR = { stateData.postValue(ViewState.Success(it)) }
            )
        }
    }
}

class KeychainPhraseViewModelFactory(
    private val getMnemonic: GetMnemonic,
    private val analytics: Analytics
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KeychainPhraseViewModel(
            getMnemonic = getMnemonic,
            analytics = analytics
        ) as T
    }
}