package com.anytypeio.anytype.ui.onboarding.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.views.Title1

@Composable
fun EnteringTheVoidScreen(
    openApp: () -> Unit,
    contentPaddingTop: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.onboarding_entering_void_title),
            style = Title1.copy(color = OnBoardingTextPrimaryColor)
        )
    }
    BackHandler {
        // nothing
    }
}