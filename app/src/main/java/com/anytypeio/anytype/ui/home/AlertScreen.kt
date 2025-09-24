package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarningLoading
import com.anytypeio.anytype.core_ui.views.HeadlineHeading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnpinWidgetScreen(
    onPinAccepted: () -> Unit,
    onPinCancelled: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onPinCancelled,
        dragHandle = {},
        containerColor = Color.Transparent,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.background_secondary),
                    shape = RoundedCornerShape(34.dp),
                )
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            AlertIcon(R.drawable.ic_popup_question_56)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.widget_unpin_alert_title),
                style = HeadlineHeading,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.widget_unpin_alert_description),
                style = BodyRegular,
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(20.dp))
            ButtonWarningLoading(
                onClick = onPinAccepted,
                size = ButtonSize.Large,
                text = stringResource(id = R.string.delete),
                modifierBox = Modifier.fillMaxWidth(),
                modifierButton = Modifier.fillMaxWidth(),
                loading = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            ButtonSecondary(
                onClick = onPinCancelled,
                size = ButtonSize.LargeSecondary,
                text = stringResource(id = R.string.cancel),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@DefaultPreviews
@Composable
fun UnpinWidgetScreenPreview() {
    UnpinWidgetScreen(
        onPinAccepted = {},
        onPinCancelled = {}
    )
}