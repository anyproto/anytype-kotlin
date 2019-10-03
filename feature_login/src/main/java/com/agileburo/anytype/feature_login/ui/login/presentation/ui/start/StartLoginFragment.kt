package com.agileburo.anytype.feature_login.ui.login.presentation.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.StartLoginSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start.StartLoginViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.start.StartLoginViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_start_login.*
import javax.inject.Inject

class StartLoginFragment : BaseFragment() {

    @Inject
    lateinit var factory: StartLoginViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(StartLoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_start_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonClicks()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    private fun setupButtonClicks() {
        signUpButton.setOnClickListener { vm.onSignUpClicked() }
        loginButton.setOnClickListener { vm.onLoginClicked() }
    }

    override fun injectDependencies() {
        StartLoginSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        StartLoginSubComponent.clear()
    }
}