package com.anytypeio.anytype.ui_settings.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui_settings.R

@Composable
fun Section(modifier: Modifier = Modifier, title: String) {
    Text(
        modifier = modifier,
        text = title,
        color = colorResource(id = R.color.text_secondary),
        fontSize = 13.sp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NameBlock(
    modifier: Modifier = Modifier,
    name: String,
    onNameSet: (String) -> Unit
) {

    val nameValue = remember { mutableStateOf(name) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.padding(start = 20.dp)) {
        Text(
            text = "Name",
            color = colorResource(id = R.color.text_secondary),
            fontSize = 13.sp
        )
        BasicTextField(
            value = nameValue.value,
            onValueChange = {
                nameValue.value = it
            },
            modifier = Modifier.padding(top = 4.dp, end = 20.dp),
            enabled = true,
            textStyle = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_primary)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onNameSet.invoke(nameValue.value)
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.OutlinedTextFieldDecorationBox(
                    value = nameValue.value,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    placeholder = {
                        Text(text = "Space name")
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = colorResource(id = R.color.text_primary),
                        backgroundColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        placeholderColor = colorResource(id = R.color.text_tertiary),
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
    }


}

@Composable
fun SpaceNameBlock(modifier: Modifier = Modifier) {
    Text(
        text = "Space",
        style = MaterialTheme.typography.h3,
        color = colorResource(id = R.color.text_primary)
    )
}

@Composable
fun SpaceImageBlock(icon: SpaceIconView, onSpaceIconClick: () -> Unit) {

    val spaceImageModifier = Modifier
        .size(96.dp)
        .clip(RoundedCornerShape(8.dp))
        .noRippleClickable {
            onSpaceIconClick.invoke()
        }

    when (icon) {
        is SpaceIconView.Emoji -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = Emojifier.uri(icon.unicode),
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Emoji space icon",
                modifier = spaceImageModifier,
                contentScale = ContentScale.Crop
            )
        }
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = spaceImageModifier
            )
        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_space),
                contentDescription = "Placeholder space icon",
                contentScale = ContentScale.Crop,
                modifier = spaceImageModifier
            )
        }
    }
}