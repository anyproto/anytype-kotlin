package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState

@Composable
fun PropertyEditScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible.Edit,
    onSaveButtonClicked: () -> Unit,
    onFormatClick: () -> Unit,
    onLimitTypesClick: () -> Unit,
    onPropertyNameUpdate: (String) -> Unit,
    onMenuUnlinkClick: (Id) -> Unit
) {

    var innerValue by remember(uiState.name) { mutableStateOf(uiState.name) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isNameChanged by remember { mutableStateOf(false) }

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
                emptyName = stringResource(R.string.untitled),
                onValueChange = {
                    innerValue = it
                    isNameChanged = true
                    onPropertyNameUpdate(it)
                }
            )
            Spacer(modifier = Modifier.size(4.dp))
            if (uiState.isPossibleToUnlinkFromType) {
                Box(
                    modifier = Modifier
                        .padding(end = 21.dp)
                        .size(40.dp)
                        .noRippleThrottledClickable {
                            isMenuExpanded = true
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center),
                        painter = painterResource(id = R.drawable.ic_widget_three_dots),
                        contentDescription = "Property menu icon",
                        contentScale = ContentScale.None,
                    )
                    DropdownMenu(
                        modifier = Modifier.defaultMinSize(minWidth = 244.dp),
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        shape = RoundedCornerShape(size = 10.dp),
                        containerColor = colorResource(id = R.color.background_primary),
                        shadowElevation = 5.dp,
                    ) {
                        DropdownMenuItem(
                            modifier = Modifier.height(44.dp),
                            onClick = {
                                onMenuUnlinkClick(uiState.id)
                                isMenuExpanded = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.property_edit_menu_unlink),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_system_red),
                                    modifier = Modifier
                                )
                            }
                        )
                    }
                }
            }
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
            PropertyLimitTypesEditSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 20.dp)
                    .noRippleThrottledClickable { onLimitTypesClick() },
                limit = uiState.limitObjectTypes.size,
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
                onSaveButtonClicked()
            },
            size = ButtonSize.Large,
            enabled = innerValue.isNotBlank() && isNameChanged
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@DefaultPreviews
@Composable
fun EditPropertyPreview() {
    PropertyEditScreen(
        modifier = Modifier.fillMaxWidth(),
        uiState = UiEditPropertyState.Visible.Edit(
            id = "dummyId1",
            key = "dummyKey1",
            name = "My property",
            formatName = "Text",
            formatIcon = R.drawable.ic_relation_format_date_small,
            limitObjectTypes = emptyList(),
            format = RelationFormat.OBJECT,
            isPossibleToUnlinkFromType = true
        ),
        onSaveButtonClicked = {},
        onFormatClick = {},
        onLimitTypesClick = {},
        onPropertyNameUpdate = {},
        onMenuUnlinkClick = {}
    )
}