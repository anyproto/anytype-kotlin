package com.anytypeio.anytype.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.DoneActionListener
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModel
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModelFactory
import com.anytypeio.anytype.presentation.common.ViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_keychain_login.*
import javax.inject.Inject

class KeychainLoginFragment : NavigationFragment(R.layout.fragment_keychain_login) {

    @Inject
    lateinit var factory: KeychainLoginViewModelFactory
    private val vm by viewModels<KeychainLoginViewModel> { factory }

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
        vm.state.observe(viewLifecycleOwner, { state ->
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
                    loginButton.isEnabled = true
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
        tvqrcode.setOnClickListener { showAlert() }
    }

    private fun showAlert() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.alert_qr_camera)
            .setPositiveButton(R.string.alert_qr_camera_ok) { dialog, _ ->
                startCamera()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun startCamera() {
        IntentIntegrator.forSupportFragment(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            vm.onGetEntropy(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun injectDependencies() {
        componentManager().keychainLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().keychainLoginComponent.release()
    }
}