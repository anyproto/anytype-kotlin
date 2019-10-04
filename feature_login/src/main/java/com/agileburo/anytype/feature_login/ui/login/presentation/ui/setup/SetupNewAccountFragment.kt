package com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.SetupNewAccountSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupNewAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupNewAccountViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_setup_new_account.*
import javax.inject.Inject

class SetupNewAccountFragment : BaseFragment() {

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
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        (activity as? CoreComponentProvider)?.let { provider ->
            SetupNewAccountSubComponent
                .get(provider.provideCoreComponent())
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        SetupNewAccountSubComponent.clear()
    }
}