package com.agileburo.anytype.ui.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.auth.congratulation.CongratulationViewModel
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_congratulation.*

class CongratulationFragment : NavigationFragment(R.layout.fragment_congratulation) {

    private val vm by lazy {
        ViewModelProviders
            .of(this)
            .get(CongratulationViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startButton.setOnClickListener { vm.onStartClicked() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
    }

    private fun setupNavigation() {
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}
}