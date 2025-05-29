package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun CreateSpaceScreen(
    spaceIconView: SpaceIconView.Placeholder,
    onCreate: (Name, IsSpaceLevelChatSwitchChecked) -> Unit,
    onSpaceIconClicked: () -> Unit,
    isLoading: State<Boolean>,
    isChatSpace: Boolean = false
) {

    var isSpaceLevelChatSwitchChecked = remember { mutableStateOf(false) }

    var innerValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                spaceIconView = spaceIconView.copy(
                    name = innerValue.text.ifEmpty {
                        stringResource(id = R.string.s)
                    }
                ),
                onSpaceIconClicked = onSpaceIconClicked
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .focusRequester(focusRequester),
                    value = innerValue,
                    onValueChange = {
                        innerValue = it
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.name),
                            style = Caption1Regular,
                            color = colorResource(id = R.color.text_secondary)
                        )
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onCreate(innerValue.text, isSpaceLevelChatSwitchChecked.value)
                        }
                    ),
                    textStyle = BodySemiBold.copy(
                        color = colorResource(id = R.color.text_primary)
                    ),
                    shape = RoundedCornerShape(size = 12.dp),
                    colors = TextFieldDefaults.colors(
                        cursorColor = colorResource(id = R.color.text_primary),
                        focusedContainerColor = colorResource(id = R.color.transparent),
                        unfocusedContainerColor = colorResource(id = R.color.transparent),
                        focusedIndicatorColor = colorResource(id = R.color.shape_primary),
                        unfocusedIndicatorColor = colorResource(id = R.color.shape_tertiary),
                    )
                )
            }
        }
        ButtonPrimaryLoading(
            onClick = {
                focusManager.clearFocus()
                keyboardController?.hide()
                onCreate(innerValue.text, isSpaceLevelChatSwitchChecked.value)
            },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierBox = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 8.dp),
            modifierButton = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            loading = isLoading.value,
            enabled = innerValue.text.isNotEmpty()
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun DebugCreateSpaceLevelChatToggle(isChatToggleChecked: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
    ) {
        Switch(
            checked = isChatToggleChecked.value,
            onCheckedChange = {
                isChatToggleChecked.value = it
            },
            colors = SwitchDefaults.colors().copy(
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
                checkedTrackColor = colorResource(R.color.palette_system_amber_50),
                uncheckedTrackColor = colorResource(R.color.shape_secondary)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Enable space-level chat (dev mode)",
            color = colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_primary),
            style = BodyRegular
        )
    }
}

@Composable
private fun CreateSpaceButton(
    modifier: Modifier,
    onCreateButtonClicked: () -> Unit,
    isLoading: State<Boolean>,
    enabled: Boolean
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .fillMaxWidth()
    ) {

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
    onSpaceIconClicked: () -> Unit
) {
    Box(modifier = modifier.wrapContentSize()) {
        SpaceIconView(
            icon = spaceIconView,
            onSpaceIconClick = onSpaceIconClicked
        )
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

typealias IsSpaceLevelChatSwitchChecked = Boolean

@DefaultPreviews
@Composable
fun CreateSpaceScreenPreview() {
    val state = remember { mutableStateOf(false) }
    CreateSpaceScreen(
        spaceIconView = SpaceIconView.Placeholder(
            color = SystemColor.RED,
            name = "My Space"
        ),
        onCreate = { _, _ -> },
        onSpaceIconClicked = {},
        isChatSpace = true,
        isLoading = state
    )
}