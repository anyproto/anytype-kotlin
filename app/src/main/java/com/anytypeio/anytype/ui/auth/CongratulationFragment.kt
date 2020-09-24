package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.auth.congratulation.CongratulationViewModel
import com.anytypeio.anytype.ui.base.NavigationFragment
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