package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.showSnackbar
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.SetupNewAccountViewModel
import com.agileburo.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_new_account.*
import javax.inject.Inject

class SetupNewAccountFragment : NavigationFragment(R.layout.fragment_setup_new_account),
    Observer<ViewState<Any>> {

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
        vm.state.observe(this, this)
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(this, navObserver)
    }

    override fun onChanged(state: ViewState<Any>) {
        when (state) {
            is ViewState.Loading -> {
                icon.startAnimation(animation)
            }
            is ViewState.Success -> {
                animation.cancel()
            }
            is ViewState.Error -> {
                animation.cancel()
                root.showSnackbar(state.error)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().setupNewAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupNewAccountComponent.release()
    }
}