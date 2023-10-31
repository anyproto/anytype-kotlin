package com.anytypeio.anytype.ui.onboarding.screens.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Title1


@Preview
@Composable
fun PreviewWhatIsRecoveryPhraseScreen() {
    WhatIsRecoveryPhraseScreen()
}

@Composable
fun WhatIsRecoveryPhraseScreen() {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(46.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier,
                text = "What is Recovery Phrase?",
                style = Title1.copy(color = OnBoardingTextPrimaryColor)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier) {
            Box(modifier = Modifier.size(56.dp)) {
                Text(
                    text = "🎲",
                    fontSize = 44.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "Recovery Phrase is 12 random words from which your account is magically generated on this device.",
                color = Color.White,
                style = BodyRegular
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier) {
            Box(modifier = Modifier.size(56.dp)) {
                Text(
                    text = "🪪",
                    fontSize = 44.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = buildAnnotatedString {
                    append("Who knows combination of these words – owns the account. ")
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append("Now, you are the only person in the world who can access it.")
                    }
                },
                color = Color.White,
                style = BodyRegular
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier) {
            Box(modifier = Modifier.size(56.dp)) {
                Text(
                    text = "☝️",
                    fontSize = 44.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = "That is why it is essential to keep Recovery Phrase secure! You own - you responsible!",
                color = Color.White,
                style = BodyRegular
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color(0xff17DAD7CA))

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_onboarding_mnemonic_phrase_lock),
                    contentDescription = "Lock icon"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "How to save my phrase?",
                    color = Color.White,
                    style = Title1.copy(
                        fontSize = 17.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(15.dp))
                Text(
                    text = "The easiest way to store your Recovery Phrase is to save it in your password manager.",
                    color = Color.White,
                    style = BodyCalloutRegular,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The most secure way is to write it down on paper and keep it offline, in a safe and secure place.",
                    color = Color.White,
                    style = BodyCalloutRegular,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}