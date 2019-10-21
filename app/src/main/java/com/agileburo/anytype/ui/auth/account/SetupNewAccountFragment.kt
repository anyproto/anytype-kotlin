package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.SetupNewAccountViewModel
import com.agileburo.anytype.presentation.auth.account.SetupNewAccountViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_setup_new_account.*
import javax.inject.Inject

class SetupNewAccountFragment : NavigationFragment(R.layout.fragment_setup_new_account) {

    @Inject
    lateinit var factory: SetupNewAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SetupNewAccountViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_setup_new_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.rotation))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(this, navObserver)
    }

    override fun injectDependencies() {
        componentManager().setupNewAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().setupNewAccountComponent.release()
    }
}