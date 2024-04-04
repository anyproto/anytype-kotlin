package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class HowToShareSpaceFragment : BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    HowToShareSpaceScreen()
                }
            }
        }
    }

    override fun injectDependencies() {
        // Do nothing
    }
    override fun releaseDependencies() {
        // Do nothing
    }
}