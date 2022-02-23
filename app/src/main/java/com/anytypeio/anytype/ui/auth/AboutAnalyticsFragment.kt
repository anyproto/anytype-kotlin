package com.anytypeio.anytype.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Announcement
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.ui.settings.typography

@ExperimentalMaterialApi
class AboutAnalyticsFragment : BaseComposeFragment() {

    private val onNextClicked = {
        findNavController().navigate(R.id.openInviteCodeScreen)
    }

    private val onBackClicked : () -> Unit = {
        findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    Announcement(
                        title = stringResource(R.string.anytype_analytics),
                        subtitle = stringResource(R.string.anytype_analytics_msg),
                        onBackClicked = onBackClicked,
                        onNextClicked = onNextClicked
                    )
                }
            }
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}