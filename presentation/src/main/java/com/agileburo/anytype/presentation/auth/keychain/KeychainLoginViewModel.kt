package com.agileburo.anytype.presentation.auth.keychain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.analytics.base.Analytics
import com.agileburo.anytype.analytics.base.EventsDictionary
import com.agileburo.anytype.analytics.event.EventAnalytics
import com.agileburo.anytype.analytics.props.Props
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.domain.auth.interactor.RecoverWallet
import com.agileburo.anytype.domain.auth.interactor.SaveMnemonic
import com.agileburo.anytype.domain.device.PathProvider
import com.agileburo.anytype.presentation.auth.congratulation.ViewState
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.launch
import timber.log.Timber

class KeychainLoginViewModel(
    private val recoverWallet: RecoverWallet,
    private val saveMnemonic: SaveMnemonic,
    private val pathProvider: PathProvider,
    private val analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    val state = MutableLiveData<ViewState<Boolean>>()

    fun onLoginClicked(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onActionDone(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onBackButtonPressed() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    private fun proceedWithRecoveringWallet(chain: String) {

        val startTime = System.currentTimeMillis()

        state.postValue(ViewState.Loading)

        recoverWallet.invoke(
            scope = viewModelScope,
            params = RecoverWallet.Params(
                path = pathProvider.providePath(),
                mnemonic = chain
            )
        ) { result ->
            val middleTime = System.currentTimeMillis()
            result.either(
                fnR = {
                        proceedWithSavingMnemonic(chain, createEvent(startTime, middleTime))
                },
                fnL = {
                    state.postValue(ViewState.Error(it.localizedMessage))
                    Timber.e(it, "Error while recovering wallet")
                }
            )
        }
    }

    private fun proceedWithSavingMnemonic(mnemonic: String, event: EventAnalytics.Anytype) {
        saveMnemonic.invoke(
            scope = viewModelScope,
            params = SaveMnemonic.Params(
                mnemonic = mnemonic
            )
        ) { result ->
            result.either(
                fnR = {
                    sendEvent(event)
                    navigation.postValue(EventWrapper(AppNavigation.Command.SelectAccountScreen))
                },
                fnL = { Timber.e(it, "Error while saving mnemonic") }
            )
        }
    }

    private fun createEvent(start: Long, middle: Long): EventAnalytics.Anytype =
        EventAnalytics.Anytype(
            name = EventsDictionary.ACCOUNT_RECOVER,
            props = Props.empty(),
            duration = EventAnalytics.Duration(
                start = start,
                middleware = middle
            )
        )

    private fun sendEvent(event: EventAnalytics.Anytype) {
        viewModelScope.launch {
            analytics.registerEvent(
                event.copy(
                    duration = event.duration?.copy(
                        render = System.currentTimeMillis()
                    )
                )
            )
        }
    }

    fun onScanQrCodeClicked() {
        // TODO
    }
}