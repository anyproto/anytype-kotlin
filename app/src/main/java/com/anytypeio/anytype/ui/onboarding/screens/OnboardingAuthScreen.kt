package com.anytypeio.anytype.ui.onboarding.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorButtonInversion
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.OnboardingSubtitleColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingTitle
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.TextOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.fontInterRegular
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel

@Preview
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onLoginClicked = {},
        onJoinClicked = {},
        onPrivacyPolicyClicked = {},
        onTermsOfUseClicked = {}
    )
}

@Composable
fun AuthScreenWrapper(
    vm: OnboardingStartViewModel
) {
    AuthScreen(
        onJoinClicked = vm::onJoinClicked,
        onLoginClicked = vm::onLoginClicked,
        onPrivacyPolicyClicked = vm::onPrivacyPolicyClicked,
        onTermsOfUseClicked = vm::onTermsOfUseClicked
    )
}

@Composable
fun AuthScreen(
    onJoinClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onTermsOfUseClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Title(modifier = Modifier)
            Subtitle(modifier = Modifier)
            Description()
            Spacer(modifier = Modifier.height(72.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            SignButtons(
                onJoinClicked = onJoinClicked,
                onLoginClicked = onLoginClicked
            )
            TermsAndPolicy(
                modifier = Modifier,
                onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                onTermsOfUseClicked = onTermsOfUseClicked
            )
        }
    }
}

@Composable
fun Title(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_auth_title),
            textAlign = TextAlign.Center,
            style = HeadlineOnBoardingTitle
                .copy(
                    color = OnBoardingTextPrimaryColor,
                    fontFamily = fontInterRegular,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W300,
                    lineHeight = 37.5.sp
                )
        )
    }
}

@Composable
fun Subtitle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_auth_subtitle),
            textAlign = TextAlign.Center,
            style = HeadlineOnBoardingTitle
                .copy(
                    color = OnboardingSubtitleColor,
                    fontSize = 43.sp,
                    lineHeight = 37.5.sp
                )
        )
    }
}


@Composable
fun Description(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 30.dp, start = 68.dp, end = 68.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_auth_description),
            textAlign = TextAlign.Center,
            style = HeadlineOnBoardingDescription.copy(color = OnBoardingTextSecondaryColor),
        )
    }
}

@Composable
fun SignButtons(
    onJoinClicked: () -> Unit,
    onLoginClicked: () -> Unit,
) {
    Column {
        OnBoardingButtonPrimary(
            text = stringResource(id = R.string.onboarding_join_button_text),
            onClick = onJoinClicked,
            enabled = true,
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp)
        )
        OnBoardingButtonSecondary(
            text = stringResource(id = R.string.onboarding_log_in_button_text),
            onClick = onLoginClicked,
            size = ButtonSize.Large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 20.dp, end = 20.dp),
            textColor = ColorButtonInversion
        )
    }
}

@Composable
fun TermsAndPolicy(
    modifier: Modifier = Modifier,
    onPrivacyPolicyClicked: () -> Unit,
    onTermsOfUseClicked: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(
            stringResource(id = R.string.onboarding_terms_and_policy_prefix)
        )

        pushStringAnnotation(tag = TermsOfUseTag, annotation = "")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.onboarding_terms_and_policy_terms))
        }
        pop()

        append(
            stringResource(id = R.string.onboarding_terms_and_policy_infix)
        )

        pushStringAnnotation(tag = PrivacyPolicyTag, annotation = "")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.onboarding_terms_and_policy_privacy))
        }
        pop()
    }

    ClickableText(
        modifier = modifier.padding(vertical = 16.dp, horizontal = 58.dp),
        text = annotatedString,
        style = TextOnBoardingDescription
            .copy(color = OnBoardingTextSecondaryColor, textAlign = TextAlign.Center),
        onClick = {
            annotatedString.getStringAnnotations(TermsOfUseTag, it, it)
                .firstOrNull()?.let {
                    onTermsOfUseClicked()
                }
            annotatedString.getStringAnnotations(PrivacyPolicyTag, it, it)
                .firstOrNull()?.let {
                    onPrivacyPolicyClicked()
                }
        }
    )
}

private const val PrivacyPolicyTag = "tag.privacy_policy"
private const val TermsOfUseTag = "tag.terms_of_use"
