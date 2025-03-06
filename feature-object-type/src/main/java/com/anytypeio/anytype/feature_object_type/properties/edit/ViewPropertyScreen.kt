package com.anytypeio.anytype.feature_object_type.properties.edit

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
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiEditPropertyState
import com.anytypeio.anytype.feature_object_type.fields.UiPropertyItemState

@Composable
fun PropertyViewScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible.View,
    fieldEvent: (FieldEvent) -> Unit
) {

    val item = uiState.item
    var innerValue by remember(item.name) { mutableStateOf(item.name) }
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
                item = item
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
            formatName = item.formatName,
            isEditable = false,
            onTypeClick = { fieldEvent(FieldEvent.OnChangeTypeClick) }
        )
        Divider()

        if (item is UiPropertyItemState.Object) {
            PropertyLimitTypesViewSection(
                limit = item.limitObjectTypesCount,
                onLimitTypesClick = { fieldEvent(FieldEvent.OnLimitTypesClick) }
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
            item = UiPropertyItemState.Object(
                id = "dummyId1",
                key = "dummyKey1",
                name = "View property",
                formatName = "Text",
                formatIcon = R.drawable.ic_relation_format_date_small,
                limitObjectTypesCount = 1
            )
        ),
        fieldEvent = {}
    )
}