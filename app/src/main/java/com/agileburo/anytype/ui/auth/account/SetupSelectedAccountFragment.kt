package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.agileburo.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.agileburo.anytype.ui.auth.Keys
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_new_account.*
import javax.inject.Inject

class SetupSelectedAccountFragment : NavigationFragment(R.layout.fragment_setup_selected_account) {

    @Inject
    lateinit var factory: SetupSelectedAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SetupSelectedAccountViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.selectAccount(arguments?.getString(Keys.SELECTED_ACCOUNT_ID_KEY) ?: throw IllegalStateException())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.rotation))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(this, navObserver)
    }

    override fun injectDependencies() {
        componentManager().setupSelectedAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupSelectedAccountComponent.release()
    }
}