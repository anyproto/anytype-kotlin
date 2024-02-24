package com.anytypeio.anytype.ui.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.multiplayer.ShareSpaceScreen
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui_settings.appearance.AppearanceScreen

class ShareSpaceFragment : BaseBottomSheetComposeFragment() {

    private val space get() = arg<String>(SPACE_ID_KEY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    ShareSpaceScreen(
                        // TODO
                    )
                }
            }
        }
    }

    override fun injectDependencies() {
        componentManager().shareSpaceComponent.get(SpaceId(space)).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().shareSpaceComponent.release()
    }

    companion object {
        const val SPACE_ID_KEY = "arg.share-space.space-id-key"
    }
}