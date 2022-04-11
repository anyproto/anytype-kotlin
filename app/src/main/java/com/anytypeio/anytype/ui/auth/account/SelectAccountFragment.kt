package com.anytypeio.anytype.ui.auth.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.databinding.FragmentSelectAccountBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModel
import com.anytypeio.anytype.presentation.auth.account.SelectAccountViewModelFactory
import com.anytypeio.anytype.ui.base.NavigationFragment
import javax.inject.Inject

class SelectAccountFragment : NavigationFragment<FragmentSelectAccountBinding>(R.layout.fragment_select_account) {

    @Inject
    lateinit var factory: SelectAccountViewModelFactory
    private val vm by viewModels<SelectAccountViewModel> { factory }

    private val profileAdapter by lazy {
        SelectAccountAdapter(
            views = mutableListOf(),
            onAddNewProfileClicked = { vm.onAddProfileClicked() },
            onProfileClicked = { vm.onProfileClicked(it.id) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpacingItemDecoration(
                    spacingTop = context.dimen(R.dimen.profile_adapter_margin_top).toInt()
                )
            )
            adapter = profileAdapter
        }

        vm.state.observe(viewLifecycleOwner) { state -> profileAdapter.update(state) }
        vm.error.observe(viewLifecycleOwner) { toast(it) }
        vm.observeNavigation().observe(viewLifecycleOwner, navObserver)
    }

    override fun injectDependencies() {
        componentManager().selectAccountComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().selectAccountComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectAccountBinding = FragmentSelectAccountBinding.inflate(
        inflater, container, false
    )
}