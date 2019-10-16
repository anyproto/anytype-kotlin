package com.agileburo.anytype.feature_profile.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.feature_profile.di.ProfileSubComponent
import com.agileburo.anytype.feature_profile.presentation.model.ProfileView
import com.agileburo.anytype.feature_profile.presentation.mvvm.ProfileViewModel
import com.agileburo.anytype.feature_profile.presentation.mvvm.ProfileViewModelFactory
import com.agileburo.anytype.feature_profile.presentation.ui.base.FeatureBaseFragment
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : FeatureBaseFragment<ViewState<ProfileView>>(R.layout.fragment_profile) {

    @Inject
    lateinit var factory: ProfileViewModelFactory

    private val vm by lazy {
        ViewModelProviders.of(this, factory).get(ProfileViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(this, this)
        vm.navigation.observe(this, navObserver)
        vm.onViewCreated()
    }

    override fun render(state: ViewState<ProfileView>) {
        when (state) {
            is ViewState.Init -> {
                logoutButton.setOnClickListener { vm.onLogoutClicked() }
                updateToggle.setOnCheckedChangeListener { _, isChecked ->
                    vm.onUpdateToggled(value = isChecked)
                }
                invitesToggle.setOnCheckedChangeListener { _, isChecked ->
                    vm.onInviteToggled(value = isChecked)
                }
                pinCodeText.setOnClickListener { vm.onPinCodeClicked() }
                keychainPhrase.setOnClickListener { vm.onKeyChainPhraseClicked() }
                backButton.setOnClickListener { vm.onBackButtonClicked() }
            }
            is ViewState.Success -> {
                name.text = state.data.name
            }
        }
    }

    override fun injectDependencies() {
        ProfileSubComponent.get(provideCoreComponent()).inject(this)
    }

    override fun releaseDependencies() {
        ProfileSubComponent.clear()
    }
}