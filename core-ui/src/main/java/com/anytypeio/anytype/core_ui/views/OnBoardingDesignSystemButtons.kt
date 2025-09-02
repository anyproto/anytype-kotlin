package com.anytypeio.anytype.core_ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.ColorButtonRegular
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryActive
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorder
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorderPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryText
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingButtonPrimary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor = if (isPressed.value) {
        colorResource(id = R.color.control_accent_125)
    } else {
        colorResource(id = R.color.control_accent)
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = colorResource(R.color.text_white),
                disabledBackgroundColor = colorResource(R.color.control_accent_25),
                disabledContentColor = colorResource(R.color.text_white)
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Box() {
                Text(
                    text = if (isLoading) "" else text,
                    style = ButtonMedium
                )
                if (isLoading) {
                    val loadingAlpha by animateFloatAsState(targetValue = 1f)
                    DotsLoadingIndicator(
                        animating = true,
                        modifier = Modifier
                            .graphicsLayer { alpha = loadingAlpha }
                            .align(Alignment.Center),
                        animationSpecs = FadeAnimationSpecs(itemCount = 3),
                        size = ButtonSize.XSmall,
                        color = ColorButtonRegular
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnBoardingButtonSecondary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    size: ButtonSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.shape_primary),
                contentColor = colorResource(id = R.color.text_primary),
                disabledBackgroundColor = colorResource(id = R.color.control_tertiary),
                disabledContentColor = colorResource(id = R.color.text_tertiary)
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            ),
            contentPadding = size.contentPadding
        ) {
            Text(
                text = text,
                style = ButtonRegular.copy(color = colorResource(R.color.text_primary))
            )
        }
    }
}