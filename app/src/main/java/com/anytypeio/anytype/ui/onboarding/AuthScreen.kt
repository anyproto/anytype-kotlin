package com.anytypeio.anytype.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.OnBoardingTextSecondaryColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingDescription
import com.anytypeio.anytype.core_ui.views.HeadlineOnBoardingTitle
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonPrimary
import com.anytypeio.anytype.core_ui.views.OnBoardingButtonSecondary
import com.anytypeio.anytype.core_ui.views.TextOnBoardingDescription


@Preview
@Composable
fun AuthScreenPreview() {
    AuthScreen {}
}

@Composable
fun AuthScreen(contract: (OnboardingScreenContract) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Title(modifier = Modifier)
            Description()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            SignButtons(contract)
            TermsAndPolicy(Modifier, contract)
        }
    }
}

@Composable
fun Title(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 226.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.on_boarding_auth_title),
            textAlign = TextAlign.Center,
            style = HeadlineOnBoardingTitle.copy(color = OnBoardingTextPrimaryColor)
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
            text = stringResource(id = R.string.on_boarding_auth_description),
            textAlign = TextAlign.Center,
            style = HeadlineOnBoardingDescription.copy(color = OnBoardingTextSecondaryColor),
        )
    }
}

@Composable
fun SignButtons(contract: (OnboardingScreenContract) -> Unit) {
    Row {
        OnBoardingButtonPrimary(
            text = stringResource(id = R.string.on_boarding_join),
            onClick = {
                contract.invoke(OnboardingScreenContract.JoinClick)
            },
            size = ButtonSize.Large,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 32.dp, end = 6.dp)
        )
        OnBoardingButtonSecondary(
            text = stringResource(id = R.string.on_boarding_log_in),
            onClick = {
                contract.invoke(OnboardingScreenContract.LogInClick)
            },
            size = ButtonSize.Large,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 6.dp, end = 32.dp)
        )
    }
}

@Composable
fun TermsAndPolicy(
    modifier: Modifier = Modifier,
    contract: (OnboardingScreenContract) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(
            stringResource(id = R.string.on_boarding_terms_and_policy_prefix)
        )

        pushStringAnnotation(tag = TermsOfUseTag, annotation = "")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.on_boarding_terms_and_policy_terms))
        }
        pop()

        append(
            stringResource(id = R.string.on_boarding_terms_and_policy_infix)
        )

        pushStringAnnotation(tag = PrivacyPolicyTag, annotation = "")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(stringResource(id = R.string.on_boarding_terms_and_policy_privacy))
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
                    contract.invoke(OnboardingScreenContract.TermsOfUseClick)
                }
            annotatedString.getStringAnnotations(PrivacyPolicyTag, it, it)
                .firstOrNull()?.let {
                    contract.invoke(OnboardingScreenContract.PrivacyPolicyClick)
                }
        }
    )
}

private const val PrivacyPolicyTag = "tag.privacy_policy"
private const val TermsOfUseTag = "tag.terms_of_use"
