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
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryActive
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryInactive
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryInactiveText
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryPressed
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryText
import com.anytypeio.anytype.core_ui.ColorButtonRegular
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryActive
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorder
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorderPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryText
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs

@Deprecated("Use OnBoardingButtonPrimary instead")
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
        ColorButtonPrimaryPressed
    } else {
        ColorButtonPrimaryActive
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor,
                contentColor = ColorButtonPrimaryText,
                disabledBackgroundColor = ColorButtonPrimaryInactive,
                disabledContentColor = ColorButtonPrimaryInactiveText
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
    size: ButtonSize,
    textColor: Color = ColorButtonSecondaryText,
    disabledBackgroundColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor = if (isPressed.value) {
        ColorButtonSecondaryPressed
    } else {
        ColorButtonSecondaryActive
    }
    val borderColor = if (isPressed.value) {
        ColorButtonSecondaryBorderPressed
    } else {
        ColorButtonSecondaryBorder
    }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = if (disabledBackgroundColor != null) {
                ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    disabledBackgroundColor = disabledBackgroundColor,
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                )
            } else {
                ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                )
            },
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
                style = size.textStyle.copy(
                    color = if (enabled) textColor else colorResource(id = R.color.text_tertiary)
                )
            )
        }
    }
}