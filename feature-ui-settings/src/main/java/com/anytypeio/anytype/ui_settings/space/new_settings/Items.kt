package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Section
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.toSpaceBackground
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.UiEvent
import com.anytypeio.anytype.presentation.spaces.UiSpaceSettingsItem
import com.anytypeio.anytype.presentation.wallpaper.WallpaperResult
import com.anytypeio.anytype.ui_settings.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
        count = item.count.toString(),
        countIsColored = item.withColor
    )
}

@Composable
fun NotificationsItem(
    modifier: Modifier = Modifier,
    icon: Int,
    supportText: String
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.notifications_title),
        icon = icon,
        count = supportText
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
fun FieldsItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.properties),
        icon = R.drawable.ic_properties_24,
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
            contentDescription = null,
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
        val spaceBackground = item.wallpaper.toSpaceBackground()
        val wallpaperModifier = Modifier
            .size(20.dp)

        when (spaceBackground) {
            is SpaceBackground.SolidColor ->
                Box(
                    modifier = wallpaperModifier
                        .background(
                            color = spaceBackground.color.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            is SpaceBackground.Gradient ->

                Box(
                    modifier = wallpaperModifier
                        .background(
                            brush = spaceBackground.brush,
                            shape = RoundedCornerShape(4.dp),
                            alpha = 0.3f
                        )
                )
            SpaceBackground.None -> {
                // Do nothing.
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
@DefaultPreviews
private fun WallpaperItemSolidColorPreview() {
    WallpaperItem(
        item = UiSpaceSettingsItem.Wallpapers(
            wallpaper = WallpaperResult.SolidColor("#3B82F6"),
            spaceIconView = SpaceIconView.DataSpace.Placeholder()
        )
    )
}

@Composable
@DefaultPreviews
private fun WallpaperItemGradientPreview() {
    WallpaperItem(
        item = UiSpaceSettingsItem.Wallpapers(
            wallpaper = WallpaperResult.Gradient("gradient-sunset"),
            spaceIconView = SpaceIconView.DataSpace.Placeholder()
        )
    )
}

@Composable
@DefaultPreviews
private fun WallpaperItemNonePreview() {
    WallpaperItem(
        item = UiSpaceSettingsItem.Wallpapers(
            wallpaper = WallpaperResult.None,
            spaceIconView = SpaceIconView.DataSpace.Placeholder()
        )
    )
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
fun RemoteStorageItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.remote_storage),
        icon = R.drawable.ic_remote_storage_24
    )
}

@Composable
fun BinItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.bin),
        icon = R.drawable.ic_widget_bin
    )
}

@Composable
@DefaultPreviews
private fun BinItemPreview() {
    BinItem()
}

@Composable
fun DeleteSpaceItem(
    modifier: Modifier = Modifier
) {
    BaseButton(
        modifier = modifier,
        title = stringResource(id = R.string.space_settings_delete_space_button),
        textColor = R.color.palette_system_red
    )
}

@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    title: String,
    count: String? = null,
    textColor: Int = R.color.text_primary,
    countIsColored: Boolean = false
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
            val (shape, color, textColor) = when {
                countIsColored && count.length > 2 -> {
                    Triple(RoundedCornerShape(100.dp), colorResource(R.color.control_accent), colorResource(id = R.color.text_white))
                }
                countIsColored -> {
                    Triple(CircleShape, colorResource(R.color.control_accent), colorResource(id = R.color.text_white))
                }
                else -> {
                    Triple(RoundedCornerShape(100.dp), Color.Transparent, colorResource(id = R.color.text_secondary))
                }
            }
            val horizontalPadding = if (count.length > 1) 5.dp else 0.dp
            Box(
                modifier = Modifier
                    .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                    .background(
                        color = color,
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = horizontalPadding),
                    text = count,
                    textAlign = TextAlign.Center,
                    style = Caption1Regular,
                    color = textColor
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_disclosure_8_24),
            contentDescription = "Members icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@DefaultPreviews
@Composable
private fun BaseButtonMembersPreview() {
    BaseButton(
        title = "Members",
        icon = R.drawable.ic_members_24,
        count = "19",
        countIsColored = true
    )
}


@OptIn(FlowPreview::class)
@Composable
fun NewSpaceNameInputField(
    modifier: Modifier = Modifier,
    name: String,
    isEditEnabled: Boolean,
    onNameSet: (String) -> Unit = {}
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(name) {
        nameValue.value = name
    }

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
        Text(
            text = stringResource(id = R.string.name),
            style = Caption1Regular.copy(color = colorResource(id = R.color.text_primary)),
            color = colorResource(id = R.color.text_secondary)
        )
        NewSettingsTextField(
            value = nameValue.value,
            textStyle = BodySemiBold.copy(color = colorResource(id = R.color.text_primary)),
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
    isEditEnabled: Boolean,
    onDescriptionSet: (String) -> Unit = {},
    allowEmptyValue: Boolean = true
) {

    val descriptionValue = remember { mutableStateOf(description) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(description) {
        descriptionValue.value = description
    }

    LaunchedEffect(descriptionValue.value) {
        snapshotFlow { descriptionValue.value }
            .debounce(300L)
            .dropWhile { input -> input == description }
            .distinctUntilChanged()
            .filter { if (allowEmptyValue) true else it.isNotEmpty() }
            .collect { query -> onDescriptionSet(query) }
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.description),
            style = Caption1Regular,
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
    textStyle: TextStyle = BodySemiBold,
    placeholderText: String,
    isEditEnabled: Boolean
) {

    val focusRequester = remember { FocusRequester() }

    val textFieldValue = TextFieldValue(
        text = value,
        selection = TextRange(value.length)
    )
    BasicTextField(
        value = textFieldValue,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .focusRequester(focusRequester)
        ,
        onValueChange = { update ->
            onValueChange(update.text)
        },
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

    LaunchedEffect(Unit) {
        // Focusing with delay, awaiting the expansion of the bottom sheet
        delay(300)
        focusRequester.requestFocus()
    }
}

@Composable
fun MultiplayerButtons(
    link: String,
    modifier: Modifier = Modifier,
    uiEvent: (UiEvent) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterHorizontally),
    ) {
        LinkItem(
            onClick = { uiEvent(UiEvent.OnShareLinkClicked(link)) },
            text = stringResource(id = R.string.space_settings_share_link),
            description = "Share link icon",
            icon = R.drawable.ic_share_link_24
        )
        LinkItem(
            onClick = { uiEvent(UiEvent.OnCopyLinkClicked(link)) },
            text = stringResource(id = R.string.space_settings_copy_link),
            description = "Copy link icon",
            icon = R.drawable.ic_copy_link_24
        )
        LinkItem(
            onClick = { uiEvent(UiEvent.OnQrCodeClicked(link)) },
            text = stringResource(id = R.string.space_settings_qrcode),
            description = "QR code icon",
            icon = R.drawable.ic_qr_code_24
        )
    }
}

@Composable
private fun RowScope.LinkItem(onClick:() -> Unit, text: String, description: String, icon: Int) {
    Column(
        modifier = Modifier
            .noRippleThrottledClickable {
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = colorResource(id = R.color.shape_transparent_secondary)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = icon),
                contentDescription = description
            )
        }
        Text(
            modifier = Modifier.wrapContentSize().padding(top = 6.dp),
            text = text,
            style = Caption2Regular,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun SpaceSettingsSection(
    modifier: Modifier = Modifier,
    item: UiSpaceSettingsItem.Section
) {
    val text = when (item) {
        UiSpaceSettingsItem.Section.Collaboration ->
            stringResource(id = R.string.space_settings_section_collaboration)

        UiSpaceSettingsItem.Section.ContentModel ->
            stringResource(id = R.string.space_settings_section_content_model)

        UiSpaceSettingsItem.Section.DataManagement ->
            stringResource(id = R.string.space_settings_section_data_management)

        UiSpaceSettingsItem.Section.Misc ->
            stringResource(id = R.string.space_settings_section_misc)

        UiSpaceSettingsItem.Section.Preferences ->
            stringResource(id = R.string.space_settings_section_preferences)
    }
    Section(
        modifier = modifier,
        title = text,
        textPaddingStart = 0.dp
    )
}

@Composable
fun AutoCreateWidgetItem(
    onCheckedStatusChanged: (Boolean) -> Unit,
    isChecked: Boolean
) {

    val checked = remember { mutableStateOf(isChecked) }

    Row(
        modifier = Modifier
            .border(
                shape = RoundedCornerShape(16.dp),
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary)
            )
            .height(64.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.space_settings_auto_create_widgets),
            style = PreviewTitle1Regular,
            color = colorResource(id = R.color.text_primary),
        )
        Switch(
            checked = checked.value,
            onCheckedChange = {
                checked.value = it
                onCheckedStatusChanged(it)
            },
            colors = SwitchDefaults.colors().copy(
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
                checkedTrackColor = colorResource(R.color.control_accent_80),
                uncheckedTrackColor = colorResource(R.color.shape_secondary)
            )
        )

    }
}

@DefaultPreviews
@Composable
private fun AutoCreateWidgetItemPreview() {
    AutoCreateWidgetItem(
        onCheckedStatusChanged = {},
        isChecked = true
    )
}

@DefaultPreviews
@Composable
private fun AutoCreateWidgetItemUncheckedPreview() {
    AutoCreateWidgetItem(
        onCheckedStatusChanged = {},
        isChecked = false
    )
}