package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.ColorButtonRegular
import com.anytypeio.anytype.core_ui.OnBoardingTextPrimaryColor
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs

@Preview
@Composable
fun EnteringTheVoidScreenPreview() {
    EnteringTheVoidScreen(
        error = "",
        contentPaddingTop = 10,
        onSystemBackPressed = {}
    )
}

@Composable
fun EnteringTheVoidScreen(
    error: String,
    contentPaddingTop: Int,
    onSystemBackPressed: () -> Unit
) {
    val loadingAlpha by animateFloatAsState(targetValue = 1f)

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
        if (error.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                textAlign = TextAlign.Center,
                text = error,
                style = TextStyle(
                    color = colorResource(id = R.color.palette_system_red),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            )
        } else {
            DotsLoadingIndicator(
                animating = true,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .graphicsLayer { alpha = loadingAlpha }
                    .align(Alignment.CenterHorizontally)
                ,
                animationSpecs = FadeAnimationSpecs(itemCount = 3),
                size = ButtonSize.XSmall,
                color = ColorButtonRegular
            )
        }
    }
    BackHandler {
        onSystemBackPressed()
    }
}