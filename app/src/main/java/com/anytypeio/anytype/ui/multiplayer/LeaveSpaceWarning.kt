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

class LeaveSpaceWarning : BaseBottomSheetComposeFragment() {

    private val name: String get() = arg(ARG_NAME)

    var onLeaveSpaceAccepted: () -> Unit = {}
    var onLeaveSpaceCancelled: () -> Unit = {}

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
                        actionButtonText = stringResource(R.string.multiplayer_leave_space),
                        cancelButtonText = stringResource(R.string.cancel),
                        title = stringResource(R.string.multiplayer_leave_space),
                        subtitle = stringResource(R.string.multiplayer_leave_space_warning_subtitle),
                        onNegativeClick = {
                            onLeaveSpaceCancelled()
                            dismiss()
                        },
                        onPositiveClick = { onLeaveSpaceAccepted() },
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
        const val ARG_NAME = "arg.leave-space-warning.name"
        fun new() : LeaveSpaceWarning = LeaveSpaceWarning().apply {
            arguments = bundleOf()
        }
    }
}