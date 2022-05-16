package com.anytypeio.anytype.ui.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.hideKeyboard
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.syncTranslationWithImeVisibility
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.DoneActionListener
import com.anytypeio.anytype.databinding.FragmentKeychainLoginBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModel
import com.anytypeio.anytype.presentation.auth.keychain.KeychainLoginViewModelFactory
import com.anytypeio.anytype.presentation.common.ViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.google.zxing.integration.android.IntentIntegrator
import javax.inject.Inject

class KeychainLoginFragment :
    NavigationFragment<FragmentKeychainLoginBinding>(R.layout.fragment_keychain_login) {

    @Inject
    lateinit var factory: KeychainLoginViewModelFactory
    private val vm by viewModels<KeychainLoginViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        setupEditTextListener()
        setupWindowInsetAnimation()
    }

    private fun setupWindowInsetAnimation() {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.container.syncTranslationWithImeVisibility()
            binding.keychainInputField.syncFocusWithImeVisibility()
        }
    }

    private fun setupEditTextListener() {
        binding.keychainInputField.setOnEditorActionListener(
            DoneActionListener(
                onActionDone = {
                    vm.onActionDone(binding.keychainInputField.text.toString())
                }
            )
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                ViewState.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }
                is ViewState.Error -> {
                    binding.loginButton.isEnabled = true
                    binding.progress.visibility = View.INVISIBLE
                    requireActivity().toast(state.error)
                }
                is ViewState.Success -> {
                    binding.loginButton.isEnabled = true
                    binding.progress.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.currentFocus?.hideKeyboard()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    private fun setupButtons() {
        binding.loginButton.setOnClickListener {
            vm.onLoginClicked(
                chain = binding.keychainInputField.text.toString()
            )
        }
        binding.backButton.setOnClickListener { vm.onBackButtonPressed() }
        binding.tvqrcode.setOnClickListener { showAlert() }
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
        IntentIntegrator.forSupportFragment(this)
            .setBeepEnabled(false)
            .initiateScan()
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

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentKeychainLoginBinding = FragmentKeychainLoginBinding.inflate(
        inflater, container, false
    )
}