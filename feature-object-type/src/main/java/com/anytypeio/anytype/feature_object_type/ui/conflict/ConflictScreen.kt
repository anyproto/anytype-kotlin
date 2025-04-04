package com.anytypeio.anytype.feature_object_type.ui.conflict

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.feature_object_type.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictScreen(
    modifier: Modifier = Modifier,
    showScreen: Boolean,
    onResetClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (showScreen) {
        ModalBottomSheet(
            modifier = modifier,
            dragHandle = {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    Dragger()
                    Spacer(modifier = Modifier.height(6.dp))
                }
            },
            scrimColor = colorResource(id = R.color.modal_screen_outside_background),
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(16.dp),
            sheetState = bottomSheetState,
            onDismissRequest = {
                onDismiss()
            }
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
                style = HeadlineSubheading,
                color = colorResource(id = R.color.text_primary),
                text = stringResource(id = R.string.object_conflict_screen_title)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center,
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_primary),
                text = stringResource(id = R.string.object_conflict_screen_description)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ButtonPrimary(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.object_conflict_screen_action_button),
                size = ButtonSize.LargeSecondary,
                onClick = {
                    onResetClick()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonSecondary(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(R.string.cancel),
                size = ButtonSize.LargeSecondary,
                onClick = {
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@DefaultPreviews
@Composable
fun ConflictScreenPreview() {
    ConflictScreen(
        showScreen = true,
        onResetClick = {},
        onDismiss = {}
    )
}