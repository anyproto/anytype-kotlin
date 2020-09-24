package com.anytypeio.anytype.features.auth.fragments

import com.anytypeio.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.anytypeio.anytype.ui.auth.account.SetupSelectedAccountFragment

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