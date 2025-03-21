package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.ui.limit_types.PropertyLimitTypesEditScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyNewScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible.New,
    onDismissLimitTypes: () -> Unit,
    onCreateNewButtonClicked: () -> Unit,
    onFormatClick: () -> Unit,
    onPropertyNameUpdate: (String) -> Unit,
    onLimitTypesClick: () -> Unit,
    onLimitObjectTypesDoneClick: (List<Id>) -> Unit
) {

    var innerValue by remember(uiState.name) { mutableStateOf(uiState.name) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.imePadding()) {
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
                isEditable = true,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                emptyName = stringResource(R.string.new_property_hint),
                onValueChange = {
                    innerValue = it
                    onPropertyNameUpdate(it)
                }
            )
            Spacer(modifier = Modifier.size(4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        PropertyFormatSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp)
                .noRippleThrottledClickable { onFormatClick() },
            formatName = uiState.formatName,
            isEditable = true,
        )
        Divider()

        if (uiState.format == RelationFormat.OBJECT) {
            PropertyLimitTypesEditSection(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp)
                    .noRippleThrottledClickable { onLimitTypesClick() },
                limit = uiState.selectedLimitTypeIds.size,
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
                onCreateNewButtonClicked()
            },
            size = ButtonSize.Large,
            enabled = innerValue.isNotBlank()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (uiState.showLimitTypes) {
        PropertyLimitTypesEditScreen(
            items = uiState.limitObjectTypes,
            savedSelectedItemIds = uiState.selectedLimitTypeIds,
            onDismissRequest = onDismissLimitTypes,
            onDoneClick = onLimitObjectTypesDoneClick
        )
    }
}

@DefaultPreviews
@Composable
fun MyPreviewNew() {
    PropertyNewScreen(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiEditPropertyState.Visible.New(
            name = "",
            formatName = "Text",
            format = RelationFormat.OBJECT,
            formatIcon = R.drawable.ic_relation_format_date_small,
            showLimitTypes = false,
            limitObjectTypes = listOf()
        ),
        onCreateNewButtonClicked = {},
        onFormatClick = {},
        onPropertyNameUpdate = {},
        onLimitTypesClick = {},
        onDismissLimitTypes = {},
        onLimitObjectTypesDoneClick = {}
    )
}