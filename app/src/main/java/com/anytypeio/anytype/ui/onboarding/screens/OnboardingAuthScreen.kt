package com.anytypeio.anytype.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonOnboardingSecondaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.fontInterRegular
import com.anytypeio.anytype.core_ui.views.fontRiccioneRegular

@Composable
fun AuthScreen(
    isLoading: Boolean,
    onSettingsClicked: () -> Unit,
    onJoinClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onTermsOfUseClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { onSettingsClicked() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_onboarding_settings),
                    contentDescription = "Onboarding settings",
                    modifier = Modifier.size(32.dp)
                )
            }
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_anytype_logo),
                contentDescription = "Anytype logo",
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            Title(modifier = Modifier.fillMaxWidth())
            Subtitle(modifier = Modifier.fillMaxWidth())
        }
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Bottom
        ) {
            SignButtons(
                isLoading = isLoading,
                onJoinClicked = onJoinClicked,
                onLoginClicked = onLoginClicked
            )
            TermsAndPolicy(
                modifier = Modifier.fillMaxWidth(),
                onPrivacyPolicyClicked = onPrivacyPolicyClicked,
                onTermsOfUseClicked = onTermsOfUseClicked
            )
        }
    }
}

@Composable
fun Title(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = colorResource(R.color.text_primary),
        text = stringResource(id = R.string.onboarding_auth_title),
        style = TextStyle(
            fontFamily = fontInterRegular,
            fontWeight = FontWeight.W400,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.05).em
        )
    )
}

@Composable
fun Subtitle(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.onboarding_auth_subtitle),
        textAlign = TextAlign.Center,
        color = colorResource(R.color.text_secondary),
        style = TextStyle(
            fontFamily = fontRiccioneRegular,
            fontWeight = FontWeight.W400,
            fontSize = 44.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.05).em
        )
    )
}

@Composable
fun SignButtons(
    isLoading: Boolean,
    onJoinClicked: () -> Unit,
    onLoginClicked: () -> Unit,
) {
    Column {
        ButtonOnboardingPrimaryLarge(
            text = stringResource(id = R.string.onboarding_new_vault_button_text),
            onClick = onJoinClicked,
            enabled = true,
            loading = isLoading,
            size = ButtonSize.Large,
            modifierBox = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
        )
        ButtonOnboardingSecondaryLarge(
            text = stringResource(id = R.string.onboarding_have_key_button_text),
            onClick = onLoginClicked,
            size = ButtonSize.Large,
            enabled = isLoading.not(),
            modifierBox = Modifier
                .padding(top = 12.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
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
        append(stringResource(id = R.string.onboarding_terms_and_policy_prefix))
        pushStringAnnotation(tag = TermsOfUseTag, annotation = "")
        append(stringResource(id = R.string.onboarding_terms_and_policy_terms))
        pop()
        append(stringResource(id = R.string.onboarding_terms_and_policy_infix))
        pushStringAnnotation(tag = PrivacyPolicyTag, annotation = "")
        append(stringResource(id = R.string.onboarding_terms_and_policy_privacy))
        pop()
    }

    ClickableText(
        modifier = modifier.padding(vertical = 16.dp, horizontal = 20.dp),
        text = annotatedString,
        style = Relations3
            .copy(color = colorResource(R.color.text_secondary), textAlign = TextAlign.Center),
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

@DefaultPreviews
@Composable
fun PreviewAuthScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))

    ) {
        Spacer(modifier = Modifier.height(40.dp))
        AuthScreen(
            isLoading = false,
            onSettingsClicked = {},
            onJoinClicked = {},
            onLoginClicked = {},
            onPrivacyPolicyClicked = {},
            onTermsOfUseClicked = {}
        )
    }
}

private const val PrivacyPolicyTag = "tag.privacy_policy"
private const val TermsOfUseTag = "tag.terms_of_use"
