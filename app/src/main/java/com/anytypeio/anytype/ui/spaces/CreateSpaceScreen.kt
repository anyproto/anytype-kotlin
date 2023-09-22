package com.anytypeio.anytype.ui.spaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title2

@Composable
fun CreateSpaceScreen(
    onCreate: (Name) -> Unit
) {
    val input = remember {
        mutableStateOf("")
    }
    Column(modifier = Modifier.fillMaxHeight()) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Header()
        Spacer(modifier = Modifier.height(16.dp))
        SpaceIcon()
        Spacer(modifier = Modifier.height(10.dp))
        SpaceNameInput(input = input)
        Divider()
        Section(title = "Type")
        TypeOfSpace()
        Divider()
        Section(title = "Start with")
        UseCase()
        Divider()
        Box(modifier = Modifier.weight(1.0f)) {
            CreateSpaceButton(
                onCreate = onCreate,
                input = input,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CreateSpaceButton(
    modifier: Modifier,
    onCreate: (Name) -> Unit,
    input: MutableState<String>
) {
    Box(
        modifier = modifier
            .height(78.dp)
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        ButtonPrimary(
            onClick = { onCreate(input.value) },
            text = stringResource(id = R.string.create),
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter)
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
fun SpaceIcon() {
    Box(modifier = Modifier
        .height(96.dp)
        .fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.Red)
                .align(Alignment.Center)
        ) {
            // TODO
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SpaceNameInput(
    input: MutableState<String>
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
    ) {
        BasicTextField(
            value = input.value,
            onValueChange = { input.value = it },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, bottom = 12.dp)
                .align(Alignment.BottomStart)
            ,
            maxLines = 1,
            singleLine = true,
            textStyle = HeadlineHeading.copy(
                color = colorResource(id = R.color.text_primary)
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
                        textColor = colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_primary),
                        backgroundColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        placeholderColor = colorResource(id = com.anytypeio.anytype.ui_settings.R.color.text_tertiary),
                        cursorColor = colorResource(id = com.anytypeio.anytype.ui_settings.R.color.orange)
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
}

@Composable
fun Section(
    title: String
) {
    Box(modifier = Modifier.height(52.dp)) {
        Text(
            modifier = Modifier
                .padding(
                    start = 20.dp,
                    bottom = 8.dp
                )
                .align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun TypeOfSpace() {
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
        Text(
            modifier = Modifier
                .padding(start = 42.dp)
                .align(Alignment.CenterStart),
            text = "Private",
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
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