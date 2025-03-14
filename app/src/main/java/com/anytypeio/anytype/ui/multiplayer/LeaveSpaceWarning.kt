package com.anytypeio.anytype.ui.multiplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.Warning
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
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

@Composable
fun LeaveSpaceWarningScreen() {
    Column {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Header(text = stringResource(R.string.multiplayer_leave_space))
        Spacer(modifier = Modifier.height(8.dp))
        AlertIcon(
            icon = AlertConfig.Icon(
                gradient = GRADIENT_TYPE_RED,
                icon = R.drawable.ic_alert_question_warning
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.multiplayer_leave_space_warning_subtitle),
            style = BodyCalloutRegular,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary),
            textAlign = TextAlign.Center
        )
        ButtonWarning(
            text = stringResource(R.string.multiplayer_leave_space),
            onClick = {},
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        )
    }
}

@Composable
fun DeleteSpaceWarningScreen() {

    var isCheckboxChecked by remember { mutableStateOf(false) }

    Column {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )
        Header(text = stringResource(R.string.delete_space))
        Spacer(modifier = Modifier.height(8.dp))
        AlertIcon(
            icon = AlertConfig.Icon(
                gradient = GRADIENT_TYPE_RED,
                icon = R.drawable.ic_alert_question_warning
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.delete_space_subtitle),
            style = BodyCalloutRegular,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .noRippleClickable {
                    isCheckboxChecked = !isCheckboxChecked
                }
        ) {
            Image(
                painter = painterResource(R.drawable.ic_checkbox_default),
                contentDescription = "Checkbox icon"
            )
            Text(
                text = "I have read and want to delete this space"
            )
        }
        ButtonWarning(
            text = stringResource(R.string.delete_space),
            onClick = {},
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 16.dp),
            isEnabled = isCheckboxChecked
        )
    }
}

@DefaultPreviews
@Composable
fun LeaveSpaceWarningScreenPreview() {
    LeaveSpaceWarningScreen()
}

@DefaultPreviews
@Composable
fun DeleteSpaceWarningScreenPreview() {
    DeleteSpaceWarningScreen()
}
