package com.agileburo.anytype.feature_login.ui.login.presentation.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.SetupSelectedAccountSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.common.Keys
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupSelectedAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.setup.SetupSelectedAccountViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_setup_selected_account.*
import javax.inject.Inject

class SetupSelectedAccountFragment : BaseFragment() {

    @Inject
    lateinit var factory: SetupSelectedAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SetupSelectedAccountViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.selectAccount(arguments?.getString(Keys.SELECTED_ACCOUNT_ID_KEY) ?: throw IllegalStateException())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_setup_selected_account, container, false)

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
        SetupSelectedAccountSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        SetupSelectedAccountSubComponent.clear()
    }
}