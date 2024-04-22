package com.anytypeio.anytype.ui.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class RemoveMemberWarning : BaseBottomSheetComposeFragment() {

    private val name: String get() = arg(ARG_NAME)

    var onAccepted: () -> Unit = {}
    var onCancelled: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    Warning(
                        actionButtonText = stringResource(R.string.remove),
                        cancelButtonText = stringResource(R.string.cancel),
                        title = stringResource(R.string.multiplayer_remove_member),
                        subtitle = stringResource(
                            id = R.string.multiplayer_remove_member_warning_text,
                            name
                        ),
                        onNegativeClick = {
                            onCancelled()
                            dismiss()
                        },
                        onPositiveClick = { onAccepted() },
                        isInProgress = false
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
        private const val ARG_NAME = "arg.leave-space-warning.name"
        fun new(name: String) : RemoveMemberWarning = RemoveMemberWarning().apply {
            arguments = bundleOf(
                ARG_NAME to name
            )
        }
    }
}