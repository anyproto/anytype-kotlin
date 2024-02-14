package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_models.PERSONAL_SPACE_TYPE
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.main.SpaceImageBlock

@Composable
fun CreateSpaceScreen(
    spaceIconView: SpaceIconView,
    onCreate: (Name) -> Unit,
    onSpaceIconClicked: () -> Unit,
    isLoading: State<Boolean>
) {
    val input = remember {
        mutableStateOf("")
    }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier.fillMaxSize()
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
            Header()
            Spacer(modifier = Modifier.height(16.dp))
            SpaceIcon(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                spaceIconView = spaceIconView,
                onSpaceIconClicked = onSpaceIconClicked
            )
            Spacer(modifier = Modifier.height(10.dp))
            SpaceNameInput(
                input = input,
                onActionDone = {
                    focusManager.clearFocus()
                    onCreate(input.value)
                }
            )
            Divider()
            Section(title = stringResource(id = R.string.type))
            TypeOfSpace(spaceType = PRIVATE_SPACE_TYPE)
            Divider()
            Section(title = stringResource(id = R.string.create_space_start_with))
            UseCase()
            Divider()
            Spacer(modifier = Modifier.height(78.dp))
        }
        CreateSpaceButton(
            onCreate = { name ->
                focusManager.clearFocus()
                onCreate(name)
            },
            input = input,
            modifier = Modifier.align(Alignment.BottomCenter),
            isLoading = isLoading
        )
    }
}

@Composable
private fun CreateSpaceButton(
    modifier: Modifier,
    onCreate: (Name) -> Unit,
    input: State<String>,
    isLoading: State<Boolean>
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .fillMaxWidth()
    ) {
        ButtonPrimaryLoading(
            onClick = { onCreate(input.value) },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifierButton = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
            ,
            modifierBox = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter)
            ,
            loading = isLoading.value
        )
    }
}

@Composable
fun Header() {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.create_space),
            style = Title2,
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
        SpaceImageBlock(
            icon = spaceIconView,
            onSpaceIconClick = onSpaceIconClicked
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpaceNameInput(
    input: MutableState<String>,
    onActionDone: () -> Unit
) {
    val focusRequester = FocusRequester()
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = input.value,
            onValueChange = { input.value = it },
            keyboardActions = KeyboardActions(
                onDone = { onActionDone() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 12.dp)
                .align(Alignment.BottomStart)
                .focusRequester(focusRequester)
            ,
            maxLines = 1,
            singleLine = true,
            textStyle = HeadlineHeading.copy(
                color = colorResource(id = R.color.text_primary)
            ),
            cursorBrush = SolidColor(
                colorResource(id = R.color.cursor_color)
            ),
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = input.value,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.space_name),
                            style = HeadlineHeading
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = colorResource(id = R.color.text_primary),
                        backgroundColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        placeholderColor = colorResource(id = R.color.text_tertiary)
                    ),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 0.dp
                    ),
                    border = {},
                    interactionSource = remember { MutableInteractionSource() },
                    visualTransformation = VisualTransformation.None
                )
            }
        )
        Text(
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Regular,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 11.dp
            ),
            text = stringResource(id = R.string.space_name)
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun Section(
    title: String,
    color: Color = colorResource(id = R.color.text_secondary)
) {
    Box(modifier = Modifier
        .height(52.dp)
        .fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(
                    start = 20.dp,
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
fun TypeOfSpace(spaceType: SpaceType?) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp),
            painter = painterResource(id = R.drawable.ic_space_type_private), 
            contentDescription = "Private space icon"
        )
        if (spaceType != null) {
            val spaceTypeName = when (spaceType) {
                PERSONAL_SPACE_TYPE -> stringResource(id = R.string.space_type_personal)
                PRIVATE_SPACE_TYPE -> stringResource(id = R.string.space_type_private)
                else -> stringResource(id = R.string.space_type_unknown)
            }
            Text(
                modifier = Modifier
                    .padding(start = 42.dp)
                    .align(Alignment.CenterStart),
                text = spaceTypeName,
                color = colorResource(id = R.color.text_primary),
                style = BodyRegular
            )
        }
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