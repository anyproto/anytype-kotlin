package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Name
import com.anytypeio.anytype.core_ui.ColorPlaceholderText
import com.anytypeio.anytype.core_ui.ColorTextInput
import com.anytypeio.anytype.core_ui.ColorTextInputCursor
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.UXBody


@Composable
private fun EmailScreen(
    onNextClicked: (Name) -> Unit,
    onSkipClicked: () -> Unit,
    onBackClicked: () -> Unit,
    isLoading: Boolean
) {
    val text = remember { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val focusRequester = FocusRequester()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(148.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.onboarding_email_add_title),
                    style = HeadlineHeading.copy(
                        color = OnBoardingTextPrimaryColor
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.onboarding_email_add_description),
                    style = UXBody.copy(
                        color = OnBoardingTextPrimaryColor
                    ),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
//                        .focusRequester(focusRequester).then(
//                            Modifier
//                                .background(
//                                    color = Color(0x26DAD7CA),
//                                    shape = RoundedCornerShape(24.dp)
//                                )
//                                .height(68.dp)
//                                .padding(horizontal = 8.dp)
//                        )
                    ,
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
                        stringResource(id = R.string.onboarding_enter_email).let {
                            Text(text = it, style = UXBody.copy(color = ColorPlaceholderText))
                        }
                    },
                    singleLine = true,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focus.clearFocus()
                            onNextClicked(text.value)
                        }
                    ),
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
//        Image(
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(top = 16.dp, start = 9.dp)
//                .noRippleClickable {
//                    focus.clearFocus()
//                    onBackClicked()
//                },
//            painter = painterResource(id = R.drawable.ic_back_onboarding_32),
//            contentDescription = stringResource(R.string.content_description_back_button_icon)
//        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp,
                    end = 20.dp,
                    bottom = 76.dp
                )
        ) {
            OnBoardingButtonPrimary(
                text = stringResource(id = R.string.onboarding_button_continue),
                onClick = {
                    onNextClicked(text.value).also {
                        focus.clearFocus(force = true)
                    }
                },
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                isLoading = isLoading
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                )
        ) {
            OnBoardingButtonSecondary(
                text = stringResource(id = R.string.onboarding_button_skip),
                onClick = {
                    onNextClicked(text.value).also {
                        focus.clearFocus(force = true)
                    }
                },
                textColor = colorResource(id = R.color.text_white),
                size = ButtonSize.Large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
        }
    }
}

@DefaultPreviews
@Composable
private fun SetProfileNameScreenPreview() {
    EmailScreen(
        onNextClicked = {},
        onBackClicked = {},
        onSkipClicked = {},
        isLoading = false
    )
}