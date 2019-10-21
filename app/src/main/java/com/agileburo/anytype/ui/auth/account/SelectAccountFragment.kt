package com.agileburo.anytype.ui.auth.account

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ui.SpacingItemDecoration
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.presentation.auth.account.SelectAccountViewModel
import com.agileburo.anytype.presentation.auth.account.SelectAccountViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_select_account.*
import javax.inject.Inject

class SelectAccountFragment : NavigationFragment(R.layout.fragment_select_account) {

    @Inject
    lateinit var factory: SelectAccountViewModelFactory

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(SelectAccountViewModel::class.java)
    }

    private val profileAdapter by lazy {
        SelectAccountAdapter(
            views = mutableListOf(),
            onAddNewProfileClicked = { vm.onAddProfileClicked() },
            onProfileClicked = { vm.onProfileClicked(it.id) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutButton.setOnClickListener { vm.onLogoutClicked() }

        profileRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpacingItemDecoration(
                    spacingTop = context.dimen(R.dimen.profile_adapter_margin_top).toInt()
                )
            )
            adapter = profileAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        vm.state.observe(this, Observer { state ->
            profileAdapter.update(state)
        })

        vm.observeNavigation().observe(this, navObserver)
    }

    override fun injectDependencies() {
        componentManager().selectAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectAccountComponent.release()
    }
}