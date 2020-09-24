package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.showSnackbar
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.anytypeio.anytype.presentation.auth.account.SetupNewAccountViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_new_account.*
import javax.inject.Inject

class SetupNewAccountFragment : NavigationFragment(R.layout.fragment_setup_new_account),
    Observer<SetupNewAccountViewState> {

    @Inject
    lateinit var factory: SetupNewAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SetupNewAccountViewModel::class.java)
    }

    private val animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
    }

    lateinit var callBack : OnBackPressedCallback

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        callBack = requireActivity().onBackPressedDispatcher.addCallback(this) {}
        setupNavigation()
        vm.state.observe(viewLifecycleOwner, this)
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    override fun onChanged(state: SetupNewAccountViewState) {
        when (state) {
            is SetupNewAccountViewState.Loading -> {
                disableBackNavigation()
                icon.startAnimation(animation)
            }
            is SetupNewAccountViewState.Success -> {
                enableBackNavigation()
                animation.cancel()
            }
            is SetupNewAccountViewState.Error -> {
                enableBackNavigation()
                animation.cancel()
                root.showSnackbar(state.message)
            }
            is SetupNewAccountViewState.InvalidCodeError -> {
                enableBackNavigation()
                animation.cancel()
                requireActivity().toast(state.message)
            }
        }
    }

    private fun disableBackNavigation() {
        callBack.isEnabled = true
    }

    private fun enableBackNavigation() {
        callBack.isEnabled = false
    }

    override fun injectDependencies() {
        componentManager().setupNewAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupNewAccountComponent.release()
    }
}