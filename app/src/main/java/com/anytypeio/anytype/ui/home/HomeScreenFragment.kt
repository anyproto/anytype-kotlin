package com.anytypeio.anytype.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import javax.inject.Inject

class HomeScreenFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: HomeScreenViewModel.Factory

    private val vm by viewModels<HomeScreenViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            HomeScreen(
                widgets = vm.views.collectAsState().value,
                onExpand = { path -> vm.onExpand(path) },
                onCreateWidget = {
                    findNavController().navigate(R.id.selectWidgetSourceScreen)
                },
                onDeleteWidget = vm::onDeleteWidgetClicked,
                onEditWidgets = { context.toast("Coming soon") },
                onRefresh = vm::onRefresh,
                onChangeWidgetSource = {
                    toast("TODO")
                },
                onChangeWidgetType = {
                    toast("TODO")
                }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun injectDependencies() {
        componentManager().homeScreenComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().homeScreenComponent.release()
    }
}