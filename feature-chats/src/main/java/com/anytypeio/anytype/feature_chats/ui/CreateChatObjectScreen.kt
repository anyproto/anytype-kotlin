package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.ext.TRANSPARENT_COLOR
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun CreateChatObjectScreen(
    icon: ObjectIcon,
    onCreateClicked: (Name) -> Unit,
    onIconUploadClicked: () -> Unit,
    onIconRemoveClicked: () -> Unit,
    onEmojiIconClicked: () -> Unit,
    isLoading: Boolean = false
) {
    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = androidx.compose.ui.text.TextRange(0)
            )
        )
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
            CreateChatHeader()
            Spacer(modifier = Modifier.height(8.dp))
            ChatObjectIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                icon = icon,
                onIconUploadClicked = onIconUploadClicked,
                onIconRemoveClicked = onIconRemoveClicked,
                onEmojiIconClicked = onEmojiIconClicked
            )
            Spacer(modifier = Modifier.height(26.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight()
            ) {
                Text(
                    text = "Name",
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
                focusManager.clearFocus()
                keyboardController?.hide()
                onCreateClicked(innerValue.text)
            },
            text = "Create",
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            loading = isLoading,
            enabled = true
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun CreateChatHeader() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Create chat",
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}

@Composable
fun ChatObjectIcon(
    modifier: Modifier,
    icon: ObjectIcon,
    onIconUploadClicked: () -> Unit,
    onIconRemoveClicked: () -> Unit,
    onEmojiIconClicked: () -> Unit
) {
    val isIconMenuExpanded = remember {
        mutableStateOf(false)
    }

    Box(
        modifier
    ) {
        Box(
            modifier = modifier
                .padding(bottom = 6.dp)
                .size(112.dp)
                .background(
                    color = colorResource(id = R.color.shape_tertiary),
                    shape = CircleShape
                )
                .noRippleClickable {
                    isIconMenuExpanded.value = true
                }
        ) {
            if (icon !is ObjectIcon.None && icon !is ObjectIcon.TypeIcon) {
                ListWidgetObjectIcon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(112.dp),
                    icon = icon,
                    iconSize = if (icon is ObjectIcon.Basic.Emoji)
                        64.dp
                    else
                        112.dp,
                    backgroundColor = R.color.transparent_black
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_create_chat_without_icon),
                    contentDescription = "Icon placeholder",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
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
                DropdownMenuItem(
                    onClick = {
                        onIconUploadClicked()
                        isIconMenuExpanded.value = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.chat_edit_info_upload_image),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
                DropdownMenuItem(
                    onClick = {
                        onEmojiIconClicked()
                        isIconMenuExpanded.value = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.emoji),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
                if (icon is ObjectIcon.Profile.Image || icon is ObjectIcon.Basic.Emoji) {
                    Divider(
                        paddingStart = 0.dp,
                        paddingEnd = 0.dp,
                    )
                    DropdownMenuItem(
                        onClick = {
                            isIconMenuExpanded.value = false
                            onIconRemoveClicked()
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.remove),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    }
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = colorResource(id = R.color.background_primary),
            tonalElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .noRippleClickable {
                    isIconMenuExpanded.value = true
                }
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_edit_chat_object_icon),
                    contentDescription = "Edit icon"
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun CreateChatObjectObjectIconPreview() {
    ChatObjectIcon(
        modifier = Modifier,
        icon = ObjectIcon.None,
        onIconUploadClicked = {},
        onIconRemoveClicked = {},
        onEmojiIconClicked = {}
    )
}

@DefaultPreviews
@Composable
fun CreateChatObjectScreenPreview() {
    CreateChatObjectScreen(
        icon = ObjectIcon.None,
        onIconUploadClicked = {},
        onIconRemoveClicked = {},
        onCreateClicked = {},
        onEmojiIconClicked = {}
    )
}