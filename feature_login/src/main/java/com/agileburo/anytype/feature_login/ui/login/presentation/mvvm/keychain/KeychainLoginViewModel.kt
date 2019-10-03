package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.keychain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.feature_login.ui.login.domain.common.PathProvider
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.RecoverWallet
import com.agileburo.anytype.feature_login.ui.login.domain.interactor.SaveMnemonic
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.NavigationCommand
import com.agileburo.anytype.feature_login.ui.login.presentation.navigation.SupportNavigation
import com.jakewharton.rxrelay2.PublishRelay
import timber.log.Timber

class KeychainLoginViewModel(
    private val recoverWallet: RecoverWallet,
    private val saveMnemonic: SaveMnemonic,
    private val pathProvider: PathProvider
) : ViewModel(), SupportNavigation {

    val state = MutableLiveData<ViewState<Boolean>>()

    private val navigation by lazy {
        PublishRelay.create<NavigationCommand>().toSerialized()
    }

    fun onLoginClicked(chain: String) {
        proceedWithRecoveringWallet(chain)
    }

    fun onActionDone(chain: String) {
        proceedWithRecoveringWallet(chain)
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
                    Timber.e(it, "Invalid mnemonic phrase")
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
                fnR = { navigation.accept(NavigationCommand.ChooseProfileScreen) },
                fnL = { Timber.e(it, "Error while saving mnemonic") }
            )
        }
    }

    fun onScanQrCodeClicked() {
        // TODO
    }

    override fun observeNavigation() = navigation
}