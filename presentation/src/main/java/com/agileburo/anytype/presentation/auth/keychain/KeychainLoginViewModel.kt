package com.agileburo.anytype.presentation.auth.keychain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.Event
import com.agileburo.anytype.domain.auth.interactor.RecoverWallet
import com.agileburo.anytype.domain.auth.interactor.SaveMnemonic
import com.agileburo.anytype.domain.auth.repo.PathProvider
import com.agileburo.anytype.presentation.auth.congratulation.ViewState
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

class KeychainLoginViewModel(
    private val recoverWallet: RecoverWallet,
    private val saveMnemonic: SaveMnemonic,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation<Event<AppNavigation.Command>> {

    override val navigation: MutableLiveData<Event<AppNavigation.Command>> = MutableLiveData()

    val state = MutableLiveData<ViewState<Boolean>>()

    fun onLoginClicked(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onActionDone(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onBackButtonPressed() {
        navigation.postValue(Event(AppNavigation.Command.Exit))
    }

    private fun proceedWithRecoveringWallet(chain: String) {

        state.postValue(ViewState.Loading)

        recoverWallet.invoke(
            scope = viewModelScope,
            params = RecoverWallet.Params(
                path = pathProvider.providePath(),
                mnemonic = chain
            )
        ) { result ->
            result.either(
                fnR = { proceedWithSavingMnemonic(chain) },
                fnL = {
                    state.postValue(ViewState.Error(it.localizedMessage))
                    Timber.e(it, "Error while recovering wallet with the following mnemonic: $chain")
                }
            )
        }
    }

    private fun proceedWithSavingMnemonic(mnemonic: String) {
        saveMnemonic.invoke(
            scope = viewModelScope,
            params = SaveMnemonic.Params(
                mnemonic = mnemonic
            )
        ) { result ->
            result.either(
                fnR = {
                    navigation.postValue(Event(AppNavigation.Command.ChooseAccountScreen))
                },
                fnL = { Timber.e(it, "Error while saving mnemonic: $mnemonic") }
            )
        }
    }

    fun onScanQrCodeClicked() {
        // TODO
    }
}