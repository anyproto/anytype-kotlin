package com.agileburo.anytype.feature_login.ui.login.presentation.ui.keychain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.KeychainLoginSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.ViewState
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.keychain.KeychainLoginViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.keychain.KeychainLoginViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.utils.DoneActionListener
import kotlinx.android.synthetic.main.fragment_keychain_login.*
import javax.inject.Inject

class KeychainLoginFragment : BaseFragment() {

    @Inject
    lateinit var factory: KeychainLoginViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(KeychainLoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_keychain_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        setupEditTextListener()
    }

    private fun setupEditTextListener() {
        keychainInputField.setOnEditorActionListener(
            DoneActionListener(
                onActionDone = {
                    vm.onActionDone(keychainInputField.text.toString())
                }
            )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
        vm.state.observe(this, Observer { state ->
            when (state) {
                ViewState.Loading -> {
                    progress.visibility = View.VISIBLE
                    loginButton.isEnabled = false
                }
                is ViewState.Error -> {
                    loginButton.isEnabled = true
                    progress.visibility = View.INVISIBLE
                    requireActivity().toast(state.error)
                }
                is ViewState.Success -> {
                    loginButton.isEnabled = false
                    progress.visibility = View.INVISIBLE
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard(activity?.currentFocus)
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    private fun setupButtons() {
        loginButton.setOnClickListener {
            vm.onLoginClicked(
                chain = keychainInputField.text.toString()
            )
        }
        qrCodeButton.setOnClickListener { vm.onScanQrCodeClicked() }
    }

    override fun injectDependencies() {
        (activity as? CoreComponentProvider)?.let { provider ->
            KeychainLoginSubComponent
                .get(provider.provideCoreComponent())
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        KeychainLoginSubComponent.clear()
    }
}