package com.anytypeio.anytype.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorMnemonicStub
import com.anytypeio.anytype.core_ui.ColorPlaceholderText
import com.anytypeio.anytype.core_ui.ColorTextInput
import com.anytypeio.anytype.core_ui.ColorTextInputCursor
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.UXBody

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MnemonicPhraseWidget(
    modifier: Modifier = Modifier,
    mnemonic: String,
    mnemonicColorPalette: List<Color>
) {
    val words = mnemonic.split(" ")
    if (words.isNotEmpty()) {
        FlowRow(
            modifier = modifier,
            maxItemsInEachRow = 4,
            horizontalArrangement = Arrangement.Center
        ) {
            words.forEachIndexed { idx, word ->
                val color = colorResource(R.color.text_primary)
                Text(
                    modifier = Modifier
                        .padding(
                            horizontal = 6.dp,
                            vertical = 6.dp
                        ),
                    text = word.lowercase(),
                    style = PreviewTitle1Medium.copy(
                        color = color
                    )
                )
            }
        }
    }
}

@Composable
fun MnemonicStub() {
    val paddingBetweenStripes = 16.dp
    val paddingAroundStripes = 24.dp
    val stripeShape = RoundedCornerShape(size = 2.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    top = paddingAroundStripes, bottom = paddingAroundStripes
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(18.dp)
                .background(color = ColorMnemonicStub, shape = stripeShape)
        )

        Spacer(modifier = Modifier.height(paddingBetweenStripes))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 62.dp)
                .height(18.dp)
                .background(color = ColorMnemonicStub, shape = stripeShape)
        )

        Spacer(modifier = Modifier.height(paddingBetweenStripes))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.dp)
                .height(18.dp)
                .background(color = ColorMnemonicStub, shape = stripeShape)
        )
    }
}

@Preview
@Composable
fun MnemonicStubPreview() {
    MnemonicStub()
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
fun MnemonicPhraseWidgetPreview() {
    val mnemonic = "kenobi hello there general grievous you are bold like toe pineapple wave"
    MnemonicPhraseWidget(
        mnemonic = mnemonic,
        mnemonicColorPalette = emptyList()
    )
}

@Composable
fun OnboardingMnemonicInput(
    modifier: Modifier = Modifier,
    text: MutableState<String>,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    TextField(
        modifier = modifier,
        textStyle = PreviewTitle1Medium
            .copy(color = colorResource(R.color.text_primary)),
        value = text.value,
        onValueChange = {
            text.value = it
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = colorResource(R.color.palette_system_blue),
            placeholderColor = colorResource(R.color.text_tertiary)
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = PreviewTitle1Regular
            )

        },
        singleLine = singleLine,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun OnboardingInput(
    modifier: Modifier = Modifier,
    text: MutableState<String>,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        modifier = modifier.then(
            Modifier
                .background(
                    color = Color(0x26DAD7CA),
                    shape = RoundedCornerShape(24.dp)
                )
                .height(68.dp)
                .padding(horizontal = 8.dp)
        ),
        textStyle = UXBody.copy(color = ColorTextInput),
        value = text.value,
        onValueChange = {
            text.value = it
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = ColorTextInputCursor
        ),
        placeholder = {
            placeholder?.let {
                Text(text = it, style = UXBody.copy(color = ColorPlaceholderText))
            }
        },
        singleLine = singleLine,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation
    )
}

@Preview
@Composable
fun OnboardingInputPreview() {
    val input = remember { mutableStateOf(String()) }
    OnboardingInput(text = input, placeholder = "My hint")
}