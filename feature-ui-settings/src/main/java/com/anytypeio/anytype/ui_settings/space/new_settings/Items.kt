package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.ui_settings.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter

@Composable
fun MembersItem(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Members
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_members_button_members),
        icon = R.drawable.ic_members_24,
        count = item.count
    )
}

@Composable
fun ObjectTypesItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_types_button),
        icon = R.drawable.ic_object_types_24,
    )
}

@Composable
fun DefaultTypeItem(
    modifier: Modifier = Modifier,
    name: String,
    icon: ObjectIcon
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.space_settings_default_type_button),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
        )
        ListWidgetObjectIcon(
            modifier = Modifier,
            iconSize = 20.dp,
            icon = icon
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = name.take(10),
            style = PreviewTitle1Regular,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun WallpaperItem(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Wallpapers
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.space_settings_wallpaper_button),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = light(item.color),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SpaceInfoItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_space_info_button),
    )
}

@Composable
fun DeleteSpaceItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_delete_space_button),
        textColor = R.color.palette_dark_red
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    title: String,
    count: Int? = null,
    textColor: Int = R.color.text_primary,
) {
    Row(
        modifier = modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .padding(vertical = 20.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = "Members icon",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = PreviewTitle1Regular,
            color = colorResource(id = textColor),
        )
        if (count != null) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .background(
                        color = colorResource(id = R.color.transparent_active),
                        shape = CircleShape
                    )
                    .padding(horizontal = 6.dp),
                text = "$count",
                textAlign = TextAlign.Center,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_white),
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun NewSpaceNameBlock(
    modifier: Modifier = Modifier,
    name: String,
    onNameSet: (String) -> Unit,
    isEditEnabled: Boolean
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(nameValue.value) {
        snapshotFlow { nameValue.value }
            .debounce(300L)
            .dropWhile { input -> input == name }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
                onNameSet(query)
            }
    }

    Column(modifier = modifier) {
        androidx.compose.material.Text(
            text = stringResource(id = R.string.space_name),
            style = BodyCalloutRegular.copy(color = colorResource(id = R.color.text_primary)),
            color = colorResource(id = R.color.text_secondary)
        )
        NewSettingsTextField(
            value = nameValue.value,
            textStyle = BodyBold.copy(color = colorResource(id = R.color.text_primary)),
            onValueChange = {
                nameValue.value = it
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            placeholderText = stringResource(id = R.string.space_settings_space_name_hint),
            isEditEnabled = isEditEnabled
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun NewSpaceDescriptionBlock(
    modifier: Modifier = Modifier,
    description: String,
    onDescriptionSet: (String) -> Unit,
    isEditEnabled: Boolean
) {

    val descriptionValue = remember { mutableStateOf(description) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(descriptionValue.value) {
        snapshotFlow { descriptionValue.value }
            .debounce(300L)
            .dropWhile { input -> input == description }
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
                onDescriptionSet(query)
            }
    }

    Column(modifier = modifier) {
        androidx.compose.material.Text(
            text = stringResource(id = R.string.space_description),
            style = BodyCalloutRegular,
            color = colorResource(id = R.color.text_secondary)
        )
        NewSettingsTextField(
            textStyle = BodyRegular.copy(color = colorResource(id = R.color.text_primary)),
            value = descriptionValue.value,
            onValueChange = {
                descriptionValue.value = it
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            placeholderText = stringResource(id = R.string.space_settings_space_description_hint),
            isEditEnabled = isEditEnabled
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewSettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = BodyBold,
    placeholderText: String,
    isEditEnabled: Boolean
) {
    BasicTextField(
        value = value,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        onValueChange = onValueChange,
        enabled = isEditEnabled,
        readOnly = !isEditEnabled,
        textStyle = textStyle,
        cursorBrush = SolidColor(colorResource(id = R.color.orange)),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = keyboardActions,
        interactionSource = remember { MutableInteractionSource() },
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.OutlinedTextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                label = null,
                leadingIcon = null,
                trailingIcon = null,
                singleLine = true,
                enabled = true,
                isError = false,
                placeholder = {
                    androidx.compose.material.Text(
                        style = textStyle,
                        text = placeholderText,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                },
                interactionSource = remember { MutableInteractionSource() },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = colorResource(id = R.color.text_primary),
                    backgroundColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    placeholderColor = colorResource(id = R.color.text_tertiary),
                    cursorColor = colorResource(id = R.color.orange)
                ),
                contentPadding = PaddingValues(),
                border = {}
            )
        }
    )
}

@Composable
@DefaultPreviews
fun MembersItemPreview() {
    Column {
        MembersItem(
            item = UiSpaceSettingsItem.Members(
                count = 5
            )
        )
    }
}