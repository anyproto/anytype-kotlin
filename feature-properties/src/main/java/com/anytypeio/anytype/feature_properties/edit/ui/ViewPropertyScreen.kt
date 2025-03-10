package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState

@Composable
fun PropertyViewScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible.View,
    onFormatClick: () -> Unit,
    onLimitTypesClick: () -> Unit
) {

    var innerValue by remember(uiState.name) { mutableStateOf(uiState.name) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            PropertyIcon(
                modifier = propertyIconModifier(),
                formatIconRes = uiState.formatIcon
            )
            PropertyName(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, top = 7.dp)
                    .weight(1.0f),
                value = innerValue,
                isEditable = false,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                emptyName = stringResource(R.string.untitled),
                onValueChange = { innerValue = it }
            )
            Spacer(modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        PropertyFormatSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp)
                .noRippleThrottledClickable { onFormatClick() },
            formatName = uiState.formatName,
            isEditable = false,
        )

        Divider()

        if (uiState.format == RelationFormat.OBJECT) {
            PropertyLimitTypesViewSection(
                limit = uiState.limitObjectTypes.size,
                onLimitTypesClick = { onLimitTypesClick() }
            )
            Divider()
        }
    }
}

@DefaultPreviews
@Composable
fun MyPreviewView() {
    PropertyViewScreen(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiEditPropertyState.Visible.View(
            id = "dummyId1",
            key = "dummyKey1",
            name = "View property",
            formatName = "Text",
            formatIcon = R.drawable.ic_relation_format_date_small,
            format = RelationFormat.FILE
        ),
        onFormatClick = {},
        onLimitTypesClick = {}
    )
}