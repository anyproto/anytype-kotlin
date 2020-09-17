package com.agileburo.anytype.ui.splash

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.BuildConfig
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.toast
import com.agileburo.anytype.core_utils.ext.visible
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.splash.SplashViewModel
import com.agileburo.anytype.presentation.splash.SplashViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_splash.*
import javax.inject.Inject

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashFragment : NavigationFragment(R.layout.fragment_splash), Observer<ViewState<Nothing>> {

    @Inject
    lateinit var factory: SplashViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SplashViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.state.observe(viewLifecycleOwner, this)
        showVersion()
    }

    override fun onResume() {
        super.onResume()
        vm.onResume()
    }

    private fun showVersion() {
        version.text = "${BuildConfig.VERSION_NAME}-alpha"
    }

    override fun onChanged(state: ViewState<Nothing>) {
        if (state is ViewState.Error) {
            toast(state.error)
            error.visible()
        }
    }

    override fun injectDependencies() {
        componentManager().splashLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().splashLoginComponent.release()
    }
}