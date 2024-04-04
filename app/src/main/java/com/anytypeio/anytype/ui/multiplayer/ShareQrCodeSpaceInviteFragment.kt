package com.anytypeio.anytype.ui.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography
import com.lightspark.composeqr.QrCodeView

class ShareQrCodeSpaceInviteFragment : BaseBottomSheetComposeFragment() {

    private val link: String get() = arg(ARG_INVITE_LINK)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    QrCodeView(
                        data = link,
                        modifier = Modifier.size(300.dp)
                    )
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

    companion object {
        const val ARG_INVITE_LINK = "arg.share-qr-code-with-invite.link"
        fun args(
            link: String
        ) = bundleOf(ARG_INVITE_LINK to link)
    }
}