package com.anytypeio.anytype.ui.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.ui.settings.typography

class DeleteSpaceWarning : BaseBottomSheetComposeFragment() {

    private val spaceId: String? get() = argOrNull<String>(ARG_SPACE_ID)

    var onDeletionAccepted: (String?) -> Unit = {}
    var onDeletionCancelled: () -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme(typography = typography) {
                    DeleteSpaceWarningScreen(
                        onDeleteClick = {
                            onDeletionAccepted(spaceId)
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
        const val ARG_SPACE_ID = "arg.space-delete-warning.space-id"
        fun new() : DeleteSpaceWarning = DeleteSpaceWarning().apply {
            arguments = bundleOf()
        }

        fun args(spaceId: String?) = bundleOf(ARG_SPACE_ID to spaceId)
    }
}

@Composable
fun DeleteSpaceWarningScreen(
    onDeleteClick: () -> Unit
) {
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
            icon = R.drawable.ic_popup_question_56
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.delete_space_subtitle),
            style = BodyCalloutRegular,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .noRippleClickable {
                    isCheckboxChecked = !isCheckboxChecked
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = if (isCheckboxChecked)
                    painterResource(R.drawable.ic_data_view_grid_checkbox_checked)
                else
                    painterResource(R.drawable.ic_data_view_grid_checkbox),
                contentDescription = "Checkbox icon"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.delete_space_checkbox_text),
                color = colorResource(R.color.text_primary),
                style = BodyCalloutRegular,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        ButtonWarning(
            text = stringResource(R.string.delete_space),
            onClick = onDeleteClick,
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
fun DeleteSpaceWarningScreenPreview() {
    DeleteSpaceWarningScreen(
        onDeleteClick = {}
    )
}
