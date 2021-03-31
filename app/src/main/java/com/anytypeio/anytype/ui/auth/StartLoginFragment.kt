package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.start.StartLoginViewModel
import com.anytypeio.anytype.presentation.auth.start.StartLoginViewModelFactory
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_start_login.*
import javax.inject.Inject

class StartLoginFragment : NavigationFragment(R.layout.fragment_start_login) {

    @Inject
    lateinit var factory: StartLoginViewModelFactory

    private val vm : StartLoginViewModel by viewModels { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.onViewCreated()
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