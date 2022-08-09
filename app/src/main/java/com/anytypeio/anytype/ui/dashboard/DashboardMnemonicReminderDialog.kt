package com.anytypeio.anytype.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.databinding.DialogDashboardKeychainPhraseBinding
import com.anytypeio.anytype.di.common.componentManager

class DashboardMnemonicReminderDialog :
    BaseMnemonicFragment<DialogDashboardKeychainPhraseBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.sendShowEvent(EventsDictionary.Type.firstSession)
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
    ): DialogDashboardKeychainPhraseBinding {
        return DialogDashboardKeychainPhraseBinding.inflate(
            inflater, container, false
        )
    }

    override val keychain: TextView by lazy { binding.keychain }
    override val btnCopy: TextView by lazy { binding.btnCopy }

    companion object {
        const val MNEMONIC_LABEL = "Your Anytype mnemonic phrase"
    }
}