package com.agileburo.anytype.ui.splash

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.splash.SplashViewModel
import com.agileburo.anytype.presentation.splash.SplashViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import javax.inject.Inject

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashFragment : NavigationFragment(R.layout.fragment_splash) {

    @Inject
    lateinit var factory: SplashViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SplashViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.navigation.observe(this, navObserver)
        vm.onViewCreated()
    }

    override fun injectDependencies() {
        componentManager().splashLoginComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().splashLoginComponent.release()
    }
}