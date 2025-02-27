package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.DebugViewModel
import javax.inject.Inject
import kotlin.getValue

class DebugFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: DebugViewModel.Factory

    private val vm by viewModels<DebugViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DebugScreen(
                    onExportAllClicked = vm::onExportWorkingDirectory
                )
            }
        }
    }

    override fun injectDependencies() {
        componentManager().debugComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().debugComponent.release()
    }
}