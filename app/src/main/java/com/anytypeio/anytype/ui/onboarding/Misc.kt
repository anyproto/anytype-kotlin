package com.anytypeio.anytype.ui.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorMnemonicPhrase
import com.anytypeio.anytype.core_ui.ColorMnemonicStub
import com.anytypeio.anytype.core_ui.ColorPagerIndicator
import com.anytypeio.anytype.core_ui.ColorPagerIndicatorCurrent
import com.anytypeio.anytype.core_ui.ColorPagerIndicatorText
import com.anytypeio.anytype.core_ui.ColorPlaceholderText
import com.anytypeio.anytype.core_ui.ColorTextInput
import com.anytypeio.anytype.core_ui.ColorTextInputCursor
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.core_ui.views.UXBody


@Composable
fun PagerIndicator(
    modifier: Modifier = Modifier, pageCount: Int, page: MutableState<OnboardingPage>, onBackClick: () -> Unit
) {
    val currentPage = remember { page }
    val screenWidth = LocalConfiguration.current.screenWidthDp.minus(32)

    if (currentPage.value.visible) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        ColorPagerIndicator, RoundedCornerShape(1.dp)
                    ), contentAlignment = Alignment.CenterStart
            ) {
                val width = (currentPage.value.num * screenWidth / pageCount).dp
                Box(
                    modifier = Modifier
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                        )
                        .width(width)
                        .height(2.dp)
                        .background(
                            ColorPagerIndicatorCurrent, RoundedCornerShape(1.dp)
                        )
                )
            }
            Row(
                modifier = Modifier.padding(
                    top = 12.dp,
                    start = 8.dp,
                    end = 0.dp
                )
            ) {
                Image(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .noRippleClickable {
                            onBackClick.invoke()
                        },
                    painter = painterResource(id = R.drawable.icon_back_onboarding),
                    contentDescription = "back"
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = "${currentPage.value.num} / $pageCount",
                    style = HeadlineOnBoardingDescription.copy(
                        color = ColorPagerIndicatorText, textAlign = TextAlign.End
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MnemonicPhraseWidget(
    modifier: Modifier = Modifier, mnemonic: String
) {
    val words = mnemonic.split(" ")
    FlowRow(
        modifier = modifier, maxItemsInEachRow = 4, horizontalArrangement = Arrangement.Center
    ) {
        words.forEach { word ->
            Text(
                modifier = Modifier.padding(horizontal = 3.dp),
                text = word.replaceFirst(word.first(), word.first().uppercaseChar()),
                style = PreviewTitle1Regular.copy(
                    color = ColorMnemonicPhrase
                )
            )
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

@Preview
@Composable
fun MnemonicPhraseWidgetPreview() {
    val mnemonic = "kenobi hello there general grievous you are bold like toe pineapple wave"
    MnemonicPhraseWidget(mnemonic = mnemonic)
}

@Composable
fun OnboardingInput(
    modifier: Modifier = Modifier,
    text: MutableState<String>,
    placeholder: String? = null,
    singleLine: Boolean = true
) {
    TextField(
        modifier = modifier.then(
            Modifier
                .background(
                    Color(0x26DAD7CA), RoundedCornerShape(24.dp)
                )
                .height(68.dp)
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
    )
}

@Preview
@Composable
fun OnboardingInputPreview() {
    val input = remember { mutableStateOf(String()) }
    OnboardingInput(text = input, placeholder = "My hint")
}