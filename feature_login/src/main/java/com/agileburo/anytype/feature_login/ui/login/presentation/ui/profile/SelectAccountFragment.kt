package com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.core_utils.di.CoreComponentProvider
import com.agileburo.anytype.core_utils.ext.dimen
import com.agileburo.anytype.core_utils.ext.disposedBy
import com.agileburo.anytype.feature_login.R
import com.agileburo.anytype.feature_login.ui.login.di.SelectProfileSubComponent
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.SelectAccountViewModel
import com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile.SelectAccountViewModelFactory
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.BaseFragment
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.SpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_choose_profile.*
import javax.inject.Inject

class SelectAccountFragment : BaseFragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_choose_profile, container, false)

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

        vm.observeNavigation().subscribe { navigation(it) }.disposedBy(subscriptions)
    }

    override fun injectDependencies() {
        (activity as? CoreComponentProvider)?.let { provider ->
            SelectProfileSubComponent
                .get(provider.provideCoreComponent())
                .inject(this)
        }
    }

    override fun releaseDependencies() {
        SelectProfileSubComponent.clear()
    }
}