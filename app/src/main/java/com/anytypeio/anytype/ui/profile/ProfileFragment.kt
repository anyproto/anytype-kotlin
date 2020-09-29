package com.anytypeio.anytype.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.avatarColor
import com.anytypeio.anytype.core_utils.ext.firstDigitByHash
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.profile.ProfileView
import com.anytypeio.anytype.presentation.profile.ProfileViewModel
import com.anytypeio.anytype.presentation.profile.ProfileViewModelFactory
import com.anytypeio.anytype.ui.base.ViewStateFragment
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : ViewStateFragment<ViewState<ProfileView>>(R.layout.fragment_profile) {

    @Inject
    lateinit var factory: ProfileViewModelFactory
    private val vm by viewModels<ProfileViewModel> { factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.onViewCreated()

        backButtonContainer.setOnClickListener { vm.onBackButtonClicked() }
    }

    override fun render(state: ViewState<ProfileView>) {
        when (state) {
            is ViewState.Init -> {
                wallpaperText.setOnClickListener {
                    vm.onWallpaperClicked()
                    toast("Coming soon...")
                }
                logoutButton.setOnClickListener { vm.onLogoutClicked() }
                pinCodeText.setOnClickListener {
                    vm.onPinCodeClicked()
                    toast("Coming soon...")
                }
                keychainPhrase.setOnClickListener { vm.onKeyChainPhraseClicked() }
                backButton.setOnClickListener { vm.onBackButtonClicked() }
                profileCardContainer.setOnClickListener { vm.onProfileCardClicked() }

                if (BuildConfig.DEBUG) {
                    with(debugSettingsButton) {
                        visible()
                        setOnClickListener { vm.onDebugSettingsClicked() }
                    }
                }
            }
            is ViewState.Success -> {
                name.text = state.data.name
                val pos = state.data.name.firstDigitByHash()
                avatar.bind(
                    name = state.data.name,
                    color = requireContext().avatarColor(pos)
                )
                state.data.avatar?.let { avatar.icon(it) }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().profileComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().profileComponent.release()
    }
}