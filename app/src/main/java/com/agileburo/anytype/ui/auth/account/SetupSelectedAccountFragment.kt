package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.SetupSelectedAccountViewModel
import com.agileburo.anytype.presentation.auth.account.SetupSelectedAccountViewModelFactory
import com.agileburo.anytype.ui.auth.Keys
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_selected_account.*
import javax.inject.Inject

open class SetupSelectedAccountFragment :
    NavigationFragment(R.layout.fragment_setup_selected_account) {

    @Inject
    lateinit var factory: SetupSelectedAccountViewModelFactory

    private val animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
    }

    private val errorObserver = Observer<String> {
        error.text = it
        animation.cancel()
    }

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
        loadingIcon.startAnimation(animation)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribe()
    }

    private fun subscribe() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
        vm.error.observe(viewLifecycleOwner, errorObserver)
    }

    override fun injectDependencies() {
        componentManager().setupSelectedAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupSelectedAccountComponent.release()
    }
}