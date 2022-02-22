package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.avatarColor
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.databinding.FragmentProfileBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.profile.ProfileView
import com.anytypeio.anytype.presentation.profile.ProfileViewModel
import com.anytypeio.anytype.presentation.profile.ProfileViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

@Deprecated("To be deleted")
class ProfileFragment : ViewStateFragment<ViewState<ProfileView>, FragmentProfileBinding>(R.layout.fragment_profile) {

    @Inject
    lateinit var factory: ProfileViewModelFactory
    private val vm by viewModels<ProfileViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.version.observe(viewLifecycleOwner) { version(it) }
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        binding.backButtonContainer.setOnClickListener { vm.onBackButtonClicked() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isLoggingOut.collect { isLoggingOut ->
                    if (isLoggingOut)
                        binding.logoutProgressBar.visible()
                    else
                        binding.logoutProgressBar.invisible()
                }
            }
        }
        vm.onViewCreated()
    }

    override fun render(state: ViewState<ProfileView>) {
        when (state) {
            is ViewState.Init -> {
                with(binding) {
                    wallpaperText.setOnClickListener {
                        findNavController().navigate(R.id.wallpaperSetScreen)
                    }
                    logoutButton.setOnClickListener { vm.onLogoutClicked() }
                    pinCodeText.setOnClickListener {
                        vm.onPinCodeClicked()
                        toast("Coming soon...")
                    }
                    keychainPhrase.setOnClickListener { vm.onKeyChainPhraseClicked() }
                    backButton.setOnClickListener { vm.onBackButtonClicked() }
                    profileCardContainer.setOnClickListener { vm.onProfileCardClicked() }
                    userSettingsText.setOnClickListener { vm.onUserSettingsClicked() }

                    if (BuildConfig.DEBUG) {
                        with(debugSettingsButton) {
                            visible()
                            setOnClickListener { vm.onDebugSettingsClicked() }
                        }
                    }
                }
            }
            is ViewState.Success -> {
                binding.name.text = state.data.name
                val pos = state.data.name.firstDigitByHash()
                binding.avatar.bind(
                    name = state.data.name,
                    color = requireContext().avatarColor(pos)
                )
                state.data.avatar?.let { binding.avatar.icon(it) }
            }
            is ViewState.Error -> {}
            ViewState.Loading -> {}
        }
    }

    private fun version(version: String) {
        if (version.isEmpty()) {
            binding.tvVersion.text = "Android v${BuildConfig.VERSION_NAME}-alpha"
        } else {
            binding.tvVersion.text = "Android v${BuildConfig.VERSION_NAME}-alpha ($version)"
        }
    }

    override fun injectDependencies() {
        componentManager().profileComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().profileComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(
        inflater, container, false
    )
}