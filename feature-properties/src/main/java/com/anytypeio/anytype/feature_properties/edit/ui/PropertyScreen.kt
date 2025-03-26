package com.anytypeio.anytype.feature_properties.edit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.dv.DragHandle
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyScreen(
    modifier: Modifier,
    uiState: UiEditPropertyState.Visible,
    onSaveButtonClicked: () -> Unit = {},
    onFormatClick: () -> Unit = {},
    onLimitTypesClick: () -> Unit = {},
    onCreateNewButtonClicked: () -> Unit = {},
    onDismissRequest: () -> Unit,
    onDismissLimitTypes: () -> Unit = {},
    onPropertyNameUpdate: (String) -> Unit,
    onMenuUnlinkClick: (Id) -> Unit ={},
    onLimitObjectTypesDoneClick: (List<Id>) -> Unit = {}
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = modifier,
        dragHandle = { DragHandle() },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_primary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = onDismissRequest,
    ) {
        when (uiState) {
            is UiEditPropertyState.Visible.Edit -> PropertyEditScreen(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                onSaveButtonClicked = onSaveButtonClicked,
                onFormatClick = onFormatClick,
                onLimitTypesClick = onLimitTypesClick,
                onPropertyNameUpdate = onPropertyNameUpdate,
                onMenuUnlinkClick = onMenuUnlinkClick,
                onDismissLimitTypes = onDismissLimitTypes
            )

            is UiEditPropertyState.Visible.View -> PropertyViewScreen(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                onFormatClick = onFormatClick,
                onLimitTypesClick = onLimitTypesClick,
                onMenuUnlinkClick = onMenuUnlinkClick,
                onDismissLimitTypes = onDismissLimitTypes
            )

            is UiEditPropertyState.Visible.New -> PropertyNewScreen(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                onCreateNewButtonClicked = onCreateNewButtonClicked,
                onFormatClick = onFormatClick,
                onPropertyNameUpdate = onPropertyNameUpdate,
                onLimitTypesClick = onLimitTypesClick,
                onDismissLimitTypes = onDismissLimitTypes,
                onLimitObjectTypesDoneClick = onLimitObjectTypesDoneClick
            )
        }
    }
}

//region Content Elements
@Composable
fun propertyIconModifier() = Modifier
    .padding(start = 20.dp)
    .size(40.dp)
    .border(
        width = 1.dp,
        color = colorResource(id = R.color.shape_primary),
        shape = RoundedCornerShape(5.dp)
    )

@Composable
fun RowScope.PropertyIcon(
    modifier: Modifier,
    formatIconRes: Int?
) {
    if (formatIconRes != null) {
        Image(
            painter = painterResource(id = formatIconRes),
            contentDescription = "Property format icon",
            contentScale = ContentScale.None,
            modifier = modifier
        )
    }
}

@Composable
fun PropertyName(
    modifier: Modifier,
    value: String,
    isEditable: Boolean,
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    emptyName: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = HeadlineHeading.copy(color = colorResource(id = R.color.text_primary)),
            singleLine = false,
            enabled = isEditable,
            maxLines = 10,

            cursorBrush = SolidColor(colorResource(id = R.color.text_primary)),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { /* You can handle focus changes here if needed */ },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions {
                keyboardController?.hide()
                focusManager.clearFocus()
                onValueChange(value)
            },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = emptyName,
                        style = HeadlineHeading,
                        color = colorResource(id = R.color.text_tertiary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun PropertyFormatSection(
    modifier: Modifier,
    formatName: String,
    isEditable: Boolean,
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(id = R.string.format),
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary)
        )
        if (isEditable) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier,
                    text = formatName,
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_secondary)
                )
                Image(
                    modifier = Modifier,
                    painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                    contentDescription = "Change field format icon"
                )
            }
        } else {
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = formatName,
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Composable
fun PropertyLimitTypesEditSection(
    modifier: Modifier,
    limit: Int,
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(end = 16.dp),
                text = stringResource(id = R.string.edit_property_limit_objects),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_primary)
            )
            val text = if (limit == 0) {
                stringResource(id = R.string.add)
            } else {
                "$limit"
            }
            Text(
                modifier = Modifier,
                text = text,
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
            Image(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                contentDescription = "Change field format icon"
            )
        }
    }
}

@Composable
fun PropertyLimitTypesViewSection(
    limit: Int,
    onLimitTypesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp)
            .noRippleThrottledClickable {
                if (limit > 0) {
                    onLimitTypesClick()
                }
            }
    ) {
        if (limit > 0) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.edit_property_limit_objects),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_primary)
            )
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier,
                    text = "$limit",
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_secondary)
                )
                Image(
                    modifier = Modifier.wrapContentSize().padding(start = 10.dp),
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = "Change field format icon"
                )
            }
        } else {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.edit_property_limit_objects_all),
                style = BodyRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_primary)
            )
            Text(
                modifier = Modifier.align(Alignment.CenterEnd),
                text = stringResource(id = R.string.edit_property_limit_all),
                style = BodyRegular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}
//endregion