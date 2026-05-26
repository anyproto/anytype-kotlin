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
import com.anytypeio.anytype.core_ui.foundation.TwoVerticalButtonsAlert
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
                    TwoVerticalButtonsAlert(
                        icon = com.anytypeio.anytype.core_ui.R.drawable.ic_popup_remove_member_56,
                        title = stringResource(R.string.multiplayer_remove_member_title),
                        description = stringResource(R.string.multiplayer_remove_member_description),
                        actionText = stringResource(R.string.multiplayer_remove_member_button),
                        cancelText = stringResource(R.string.cancel),
                        isActionDestructive = true,
                        onActionClicked = { onAccepted() },
                        onCancelClicked = {
                            onCancelled()
                            dismiss()
                        }
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