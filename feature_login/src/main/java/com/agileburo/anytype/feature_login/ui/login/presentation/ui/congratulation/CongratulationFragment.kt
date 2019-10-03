package com.agileburo.anytype.feature_login.ui.login.presentation.ui.congratulation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.core_utils.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.CongratulationSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.congratulation.CongratulationViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_congratulation.*

class CongratulationFragment : BaseFragment() {

    private val vm by lazy {
        ViewModelProviders
            .of(this)
            .get(CongratulationViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_congratulation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startButton.setOnClickListener { vm.onStartClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        CongratulationSubComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        CongratulationSubComponent.clear()
    }
}