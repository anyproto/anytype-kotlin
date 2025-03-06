package com.anytypeio.anytype.feature_object_type.properties.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiEditPropertyState
import com.anytypeio.anytype.feature_object_type.fields.UiPropertyItemState

@Composable
fun PropertyEditScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible.Edit,
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
                isEditable = true,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                emptyName = item.emptyName,
                onValueChange = { innerValue = it }
            )
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .padding(end = 21.dp)
                    .size(40.dp)
                    .noRippleThrottledClickable {

                    }
            ) {
                Image(
                    modifier = Modifier
                        //.padding(end = 20.dp)
                        .wrapContentSize()
                        .align(Alignment.Center),
                    painter = painterResource(id = R.drawable.ic_widget_three_dots),
                    contentDescription = "Property menu icon",
                    contentScale = androidx.compose.ui.layout.ContentScale.None,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Field type section
        FieldTypeSection(
            formatName = item.formatName,
            isEditable = true,
            onTypeClick = { fieldEvent(FieldEvent.OnChangeTypeClick) }
        )
        Divider()

        // Limit object types (only for OBJECT format)
        if (item is UiPropertyItemState.Object) {
            LimitTypesSectionEditState(
                limit = item.limitObjectTypesCount,
                onLimitTypesClick = { fieldEvent(FieldEvent.OnLimitTypesClick) }
            )
            Divider()
        }

        Spacer(modifier = Modifier.height(14.dp))

        ButtonPrimary(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            text = stringResource(R.string.object_type_fields_btn_save),
            onClick = {

            },
            size = ButtonSize.Large
        )
    }
}

@DefaultPreviews
@Composable
fun EditPropertyPreview() {
    PropertyEditScreen(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiEditPropertyState.Visible.Edit(
            item = UiPropertyItemState.Object(
                id = "dummyId1",
                name = "My property",
                emptyName = "Empty name",
                formatName = "Text",
                formatIcon = R.drawable.ic_relation_format_date_small,
                limitObjectTypesCount = 3
            )
        ),
        fieldEvent = {}
    )
}