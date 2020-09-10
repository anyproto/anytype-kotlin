package com.agileburo.anytype.ui.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.hideKeyboard
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ui.DoneActionListener
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.congratulation.ViewState
import com.agileburo.anytype.presentation.auth.keychain.KeychainLoginViewModel
import com.agileburo.anytype.presentation.auth.keychain.KeychainLoginViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_keychain_login.*
import javax.inject.Inject

class KeychainLoginFragment : NavigationFragment(R.layout.fragment_keychain_login) {

    @Inject
    lateinit var factory: KeychainLoginViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(KeychainLoginViewModel::class.java)
    }

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
        vm.state.observe(viewLifecycleOwner, Observer { state ->
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
        activity?.currentFocus?.hideKeyboard()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    private fun setupButtons() {
        loginButton.setOnClickListener {
            vm.onLoginClicked(
                chain = keychainInputField.text.toString()
            )
        }
        backButton.setOnClickListener { vm.onBackButtonPressed() }
    }

    override fun injectDependencies() {
        componentManager().keychainLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().keychainLoginComponent.release()
    }
}