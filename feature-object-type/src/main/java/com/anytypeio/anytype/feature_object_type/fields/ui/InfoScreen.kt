package com.anytypeio.anytype.feature_object_type.fields.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionLocalFieldsInfo(
    modifier: Modifier,
    state: UiLocalsFieldsInfoState,
    fieldEvent: (FieldEvent) -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (state is UiLocalsFieldsInfoState.Visible) {
        LocalInfoScreen(
            modifier = modifier,
            bottomSheetState = bottomSheetState,
            onDismiss = {
                fieldEvent(FieldEvent.FieldLocalInfo.OnDismiss)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalInfoScreen(
    modifier: Modifier,
    bottomSheetState: SheetState,
    @StringRes title : Int = R.string.object_type_fields_local_info_title,
    @StringRes description : Int = R.string.object_type_fields_local_info_description,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        dragHandle = { DragHandle() },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_primary),
        shape = RoundedCornerShape(16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            textAlign = TextAlign.Center,
            style = HeadlineHeading,
            color = colorResource(id = R.color.text_primary),
            text = stringResource(title)
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            textAlign = TextAlign.Center,
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_primary),
            text = stringResource(description)
        )
        Spacer(modifier = Modifier.height(30.dp))
        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(R.string.object_type_fields_local_info_button),
            size = ButtonSize.Large,
            onClick = {
                onDismiss()
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@DefaultPreviews
@Composable
fun SectionLocalFieldsInfoPreview() {
    SectionLocalFieldsInfo(
        modifier = Modifier.fillMaxWidth(),
        state = UiLocalsFieldsInfoState.Visible,
        fieldEvent = {}
    )
}