package com.agileburo.anytype.features.auth.fragments

import com.agileburo.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.agileburo.anytype.ui.auth.account.SetupSelectedAccountFragment

class TestSetupSelectedAccountFragment : SetupSelectedAccountFragment() {

    init {
        factory = testViewModelFactory
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}

    companion object {
        lateinit var testViewModelFactory: SetupSelectedAccountViewModelFactory
    }
}