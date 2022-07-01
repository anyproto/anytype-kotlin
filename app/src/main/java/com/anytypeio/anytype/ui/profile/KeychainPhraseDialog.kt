package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.databinding.DialogKeychainPhraseBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.dashboard.BaseMnemonicFragment

class KeychainPhraseDialog : BaseMnemonicFragment<DialogKeychainPhraseBinding>() {

    private val screenType get() = arg<String>(ARG_SCREEN_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.sendShowEvent(screenType)
    }

    override fun injectDependencies() {
        componentManager().keychainPhraseComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().keychainPhraseComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogKeychainPhraseBinding = DialogKeychainPhraseBinding.inflate(
        inflater, container, false
    )

    override val keychain: TextView by lazy { binding.keychain }

    companion object {
        const val ARG_SCREEN_TYPE = "arg.keychain.screen.type"
    }
}