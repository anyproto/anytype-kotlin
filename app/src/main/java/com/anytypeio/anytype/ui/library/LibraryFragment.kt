package com.anytypeio.anytype.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.google.accompanist.pager.ExperimentalPagerApi
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview

class LibraryFragment : BaseComposeFragment() {

    @Inject
    lateinit var factory: LibraryViewModel.Factory

    private val vm by viewModels<LibraryViewModel> { factory }

    @FlowPreview
    @ExperimentalLifecycleComposeApi
    @ExperimentalPagerApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    LibraryScreen(
                        listOf(
                            LibraryScreenConfig.Types(),
                            LibraryScreenConfig.Relations()
                        ), vm
                    )
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().libraryComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().libraryComponent.release()
    }

}