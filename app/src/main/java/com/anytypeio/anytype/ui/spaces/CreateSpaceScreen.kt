package com.anytypeio.anytype.ui.spaces

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun CreateSpaceScreen(
    spaceIconView: SpaceIconView,
    onCreate: (Name) -> Unit,
    onSpaceIconUploadClicked: () -> Unit,
    onSpaceIconRemoveClicked: () -> Unit,
    isLoading: State<Boolean>
) {
    val isChatSpace = remember { spaceIconView is SpaceIconView.ChatSpace }
    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(16.dp)
            )
            .imePadding()
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
        ) {
            Header(isChatSpace = isChatSpace)
            Spacer(modifier = Modifier.height(8.dp))
            SpaceIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                spaceIconView = when (spaceIconView) {
                    is SpaceIconView.DataSpace.Placeholder -> spaceIconView.copy(
                        name = innerValue.text.ifEmpty {
                            stringResource(id = R.string.u)
                        }
                    )
                    is SpaceIconView.ChatSpace.Placeholder -> spaceIconView.copy(
                        name = innerValue.text.ifEmpty {
                            stringResource(id = R.string.u)
                        }
                    )
                    else -> spaceIconView
                },
                onSpaceIconUploadClicked = onSpaceIconUploadClicked,
                onSpaceIconRemoveClicked = onSpaceIconRemoveClicked
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.create_space_change_icon),
                style = BodyCalloutMedium,
                color = colorResource(id = R.color.glyph_active)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = if (isChatSpace) stringResource(id = R.string.create_space_chat_name) else stringResource(id = R.string.create_space_space_name),
                    style = Caption1Medium,
                    color = colorResource(id = R.color.text_secondary)
                )
                OutlinedTextField(
                    value = innerValue,
                    onValueChange = {
                        innerValue = it
                    },
                    textStyle = BodySemiBold.copy(
                        color = colorResource(id = R.color.text_primary)
                    ),
                    singleLine = true,
                    enabled = true,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = colorResource(id = R.color.text_primary),
                        cursorColor = colorResource(id = R.color.color_accent),
                        focusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        unfocusedContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        errorContainerColor = colorResource(id = R.color.shape_transparent_secondary),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 0.dp, top = 12.dp)
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    shape = RoundedCornerShape(size = 26.dp),
                    placeholder = {
                        Text(
                            modifier = Modifier.padding(start = 1.dp),
                            text = stringResource(id = R.string.untitled),
                            style = BodySemiBold,
                            color = colorResource(id = R.color.text_tertiary)
                        )
                    }
                )
            }
        }
        ButtonOnboardingPrimaryLarge(
            onClick = {
                if (isChatSpace || innerValue.text.isNotEmpty()) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onCreate(innerValue.text)
                }
            },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            loading = isLoading.value,
            enabled = isChatSpace || innerValue.text.isNotEmpty()
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun Header(isChatSpace: Boolean = false) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = if (isChatSpace) R.string.create_chat else R.string.create_space),
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun SpaceIcon(
    modifier: Modifier,
    spaceIconView: SpaceIconView,
    onSpaceIconUploadClicked: () -> Unit,
    onSpaceIconRemoveClicked: () -> Unit,
) {
    val context = LocalContext.current

    val isIconMenuExpanded = remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier.wrapContentSize()) {
        SpaceIconView(
            icon = spaceIconView,
            onSpaceIconClick = {
                isIconMenuExpanded.value = !isIconMenuExpanded.value
            }
        )
        DropdownMenu(
            modifier = Modifier,
            expanded = isIconMenuExpanded.value,
            offset = DpOffset(x = 0.dp, y = 6.dp),
            onDismissRequest = {
                isIconMenuExpanded.value = false
            },
            shape = RoundedCornerShape(10.dp),
            containerColor = colorResource(id = R.color.background_secondary)
        ) {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)) {
                DropdownMenuItem(
                    onClick = {
                        onSpaceIconUploadClicked()
                        isIconMenuExpanded.value = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.profile_settings_apply_upload_image),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
            if (spaceIconView is SpaceIconView.ChatSpace.Image
                || spaceIconView is SpaceIconView.DataSpace.Image) {
                Divider(
                    paddingStart = 0.dp,
                    paddingEnd = 0.dp,
                )
                DropdownMenuItem(
                    onClick = {
                        isIconMenuExpanded.value = false
                        onSpaceIconRemoveClicked()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.remove_image),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
        }
    }
}

@Composable
fun Section(
    title: String,
    color: Color = colorResource(id = R.color.text_secondary),
    textPaddingStart: Dp = 20.dp
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(
                    start = textPaddingStart,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = color,
            style = Caption1Regular
        )
    }
}

@Composable
fun UseCase() {
    Box(modifier = Modifier.height(52.dp)) {
        Text(
            modifier = Modifier
                .padding(start = 20.dp)
                .align(Alignment.CenterStart),
            text = "Empty space",
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
    }
}

@DefaultPreviews
@Composable
fun CreateSpaceScreenPreview() {
    val state = remember { mutableStateOf(false) }
    CreateSpaceScreen(
        spaceIconView = SpaceIconView.ChatSpace.Placeholder(
            color = SystemColor.RED,
            name = "My Space"
        ),
        onCreate = { },
        onSpaceIconUploadClicked = {},
        onSpaceIconRemoveClicked = {},
        isLoading = state
    )
}