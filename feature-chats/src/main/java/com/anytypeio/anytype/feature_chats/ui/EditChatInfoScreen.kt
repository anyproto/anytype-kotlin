package com.anytypeio.anytype.feature_chats.ui

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.feature_chats.presentation.ChatInfoUpdate
import com.anytypeio.anytype.feature_chats.presentation.ChatObjectIcon

sealed class ChatInfoScreenState {
    data object Create : ChatInfoScreenState()
    data class Edit(
        val currentName: String,
        val currentIcon: ObjectIcon
    ) : ChatInfoScreenState()
}

@Composable
fun EditChatInfoScreen(
    state: ChatInfoScreenState,
    icon: ObjectIcon,
    selectedIcon: ChatObjectIcon = ChatObjectIcon.None,
    onSave: (ChatInfoUpdate) -> Unit,
    onCreate: (Name) -> Unit,
    onIconUploadClicked: () -> Unit,
    onIconRemoveClicked: () -> Unit,
    onEmojiIconClicked: () -> Unit = {},
    isLoading: Boolean = false
) {
    val isEditMode = state is ChatInfoScreenState.Edit
    val isCreateMode = state is ChatInfoScreenState.Create
    
    val initialValue = if (state is ChatInfoScreenState.Edit) {
        state.currentName
    } else {
        ""
    }
    
    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = initialValue,
                selection = androidx.compose.ui.text.TextRange(initialValue.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Resolve icon to display based on selection
    val displayIcon: ObjectIcon = when (selectedIcon) {
        is ChatObjectIcon.Image -> ObjectIcon.Profile.Image(hash = selectedIcon.uri, name = initialValue)
        is ChatObjectIcon.Emoji -> ObjectIcon.Basic.Emoji(unicode = selectedIcon.unicode)
        ChatObjectIcon.Removed -> ObjectIcon.None
        ChatObjectIcon.None -> icon
    }

    // Detect if anything has changed
    val hasNameChanged = isEditMode && innerValue.text != initialValue
    val hasIconChanged = isEditMode && selectedIcon != ChatObjectIcon.None
    val isModified = hasNameChanged || hasIconChanged
    val isSaveEnabled = if (isEditMode) isModified else true

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
            Header(isEditMode = isEditMode, isCreateMode = isCreateMode)
            Spacer(modifier = Modifier.height(8.dp))
            ChatObjectIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                icon = displayIcon,
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
                    text = "Chat name",
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
                if (isEditMode) {
                    onSave(ChatInfoUpdate(name = innerValue.text, chatIcon = selectedIcon))
                } else if (isCreateMode) {
                    onCreate(innerValue.text)
                }
            },
            text = if (isEditMode) "Save" else "Create",
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            loading = isLoading,
            enabled = isSaveEnabled
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun Header(isEditMode: Boolean, isCreateMode: Boolean) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = when {
                isEditMode -> stringResource(R.string.chat_edit_info)
                isCreateMode -> stringResource(R.string.create_chat)
                else -> ""
            },
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
    }
}
