package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.HeadlineTitleSemibold


@DefaultPreviews
@Composable
fun PreviewWhatIsRecoveryPhraseScreen() {
    WhatIsRecoveryPhraseScreen()
}

@Composable
fun WhatIsRecoveryPhraseScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(44.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.onboarding_what_is_the_key),
                style = HeadlineTitleSemibold,
                color = colorResource(id = R.color.text_primary),
                letterSpacing = (-0.48).sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier.size(56.dp),
            painter = painterResource(id = R.drawable.ic_mnemonic_dice),
            contentDescription = "Shield icon"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description_title),
            color = colorResource(id = R.color.text_primary),
            style = BodySemiBold,
            letterSpacing = (-0.41).sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description),
            color = colorResource(id = R.color.text_secondary),
            style = BodyCalloutRegular
        )

        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier.size(56.dp),
            painter = painterResource(id = R.drawable.ic_mnemonic_face),
            contentDescription = "Shield icon"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description_2_title),
            color = colorResource(id = R.color.text_primary),
            style = BodySemiBold,
            letterSpacing = (-0.41).sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description_2),
            color = colorResource(id = R.color.text_secondary),
            style = BodyCalloutRegular
        )

        Spacer(modifier = Modifier.height(24.dp))
        Image(
            modifier = Modifier.size(56.dp),
            painter = painterResource(id = R.drawable.ic_mnemonic_safe),
            contentDescription = "Shield icon"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description_3_title),
            color = colorResource(id = R.color.text_primary),
            style = BodySemiBold,
            letterSpacing = (-0.41).sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.onboarding_recovery_phrase_description_3),
            color = colorResource(id = R.color.text_secondary),
            style = BodyCalloutRegular
        )
        Spacer(modifier = Modifier.height(106.dp))
    }
}