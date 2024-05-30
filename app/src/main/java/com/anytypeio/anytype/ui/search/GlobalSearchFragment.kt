package com.anytypeio.anytype.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_utils.ext.setupBottomSheetBehavior
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject

class GlobalSearchFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: GlobalSearchViewModel.Factory

    private val vm by viewModels<GlobalSearchViewModel> { factory }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                GlobalSearchScreen(
                    items = vm.views.collectAsStateWithLifecycle().value,
                    onQueryChanged = vm::onQueryChanged,
                    onObjectClicked = vm::onObjectClicked
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior(0)
    }

    override fun injectDependencies() {
        componentManager().globalSearchComponent.get().inject(this)
    }
    override fun releaseDependencies() {
        componentManager().globalSearchComponent.release()
    }
}