package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun Section(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        color = colorResource(id = R.color.text_secondary),
        style = Caption1Regular
    )
}

@OptIn(FlowPreview::class)
@Composable
fun SpaceNameBlock(
    modifier: Modifier = Modifier,
    name: String,
    onNameSet: (String) -> Unit
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(nameValue.value) {
        snapshotFlow { nameValue.value }
            .debounce(SPACE_NAME_CHANGE_DELAY)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { query ->
               onNameSet(query)
            }
    }

    Column(modifier = modifier.padding(start = 20.dp)) {
        Text(
            text = stringResource(id = R.string.space_name),
            style = Caption1Regular,
            color = colorResource(id = R.color.text_secondary)
        )
        SettingsTextField(
            value = nameValue.value,
            onValueChange = {
                nameValue.value = it
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
        )
    }
}

@Composable
fun SpaceNameBlock() {
    Text(
        text = stringResource(id = R.string.personal_space),
        style = Title1,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
fun SpaceImageBlock(
    icon: SpaceIconView?,
    onSpaceIconClick: () -> Unit,
    mainSize: Dp = 96.dp,
    emojiSize: Dp = 48.dp,
    gradientSize: Dp = 64.dp
) {
    when (icon) {
        is SpaceIconView.Emoji -> {
            Box(
                modifier = Modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(16.dp))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
                    .background(color = colorResource(id = R.color.glyph_selected))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = Emojifier.safeUri(icon.unicode),
                        error = painterResource(id = R.drawable.ic_home_widget_space)
                    ),
                    contentDescription = "Emoji space icon",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(emojiSize)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(8.dp))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
            )
        }
        is SpaceIconView.Gradient -> {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color(icon.from.toColorInt()),
                    Color(icon.to.toColorInt())
                )
            )
            Box(
                modifier = Modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = colorResource(id = R.color.glyph_selected))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(gradientSize)
                        .clip(RoundedCornerShape(32.dp))
                        .background(gradient)
                )
            }

        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_space),
                contentDescription = "Placeholder space icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(8.dp))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
            )
        }
    }
}

@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {

    @OptIn(ExperimentalMaterialApi::class)
    BasicTextField(
        value = value,
        modifier = Modifier
            .padding(top = 4.dp, end = 20.dp)
            .fillMaxWidth(),
        onValueChange = onValueChange,
        enabled = true,
        readOnly = false,
        textStyle = HeadlineHeading.copy(color = colorResource(id = R.color.text_primary)),
        cursorBrush = SolidColor(colorResource(id = R.color.orange)),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = keyboardActions,
        interactionSource = remember { MutableInteractionSource() },
        singleLine = true,
        maxLines = 1,
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
                    Text(text = stringResource(id = R.string.space_name))
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

private const val SPACE_NAME_CHANGE_DELAY = 300L