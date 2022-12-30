package com.anytypeio.anytype.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.databinding.FragmentHomeBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.HomeViewModel
import com.anytypeio.anytype.ui.base.NavigationFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFragment : NavigationFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    @Inject
    lateinit var factory: HomeViewModel.Factory

    private val vm by viewModels<HomeViewModel> { factory }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            vm.msg.collect {
                binding.tvMsg.text = it
            }
        }
    }

    override fun injectDependencies() {
        componentManager().homescreenComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().homescreenComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(
        inflater, container, false
    )
}