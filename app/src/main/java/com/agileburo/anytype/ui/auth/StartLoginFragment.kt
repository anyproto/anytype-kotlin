package com.agileburo.anytype.ui.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.start.StartLoginViewModel
import com.agileburo.anytype.presentation.auth.start.StartLoginViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_start_login.*
import javax.inject.Inject

class StartLoginFragment : NavigationFragment(R.layout.fragment_start_login) {

    @Inject
    lateinit var factory: StartLoginViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(StartLoginViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonClicks()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    private fun setupButtonClicks() {
        signUpButton.setOnClickListener { vm.onSignUpClicked() }
        loginButton.setOnClickListener { vm.onLoginClicked() }
    }

    override fun injectDependencies() {
        componentManager().startLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().startLoginComponent.release()
    }
}