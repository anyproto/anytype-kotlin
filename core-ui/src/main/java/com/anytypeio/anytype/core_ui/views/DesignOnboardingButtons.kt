package com.anytypeio.anytype.core_ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonOnboardingPrimaryLarge(
    text: String = "",
    modifierBox: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.control_accent_125)
        else colorResource(id = R.color.control_accent)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                enabled = enabled,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    contentColor = colorResource(id = R.color.text_white),
                    disabledBackgroundColor = colorResource(id = R.color.control_tertiary),
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = PaddingValues(0.dp, 14.dp, 0.dp, 14.dp)
            ) {
                Text(
                    text = if (loading) "" else text,
                    modifier = Modifier,
                    style = ButtonMedium,
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.text_white),
                size = size
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonOnboardingSecondaryLarge(
    text: String = "",
    modifierBox: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.control_tertiary)
        else colorResource(id = R.color.shape_primary)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                enabled = enabled,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = backgroundColor,
                    contentColor = colorResource(id = R.color.text_primary),
                    disabledBackgroundColor = colorResource(id = R.color.shape_tertiary),
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = PaddingValues(0.dp, 14.dp, 0.dp, 14.dp)
            ) {
                Text(
                    text = if (loading) "" else text,
                    modifier = Modifier,
                    style = ButtonMedium,
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.text_white),
                size = size
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonOnboardingLinkLarge(
    text: String = "",
    modifierBox: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingItemsCount: Int = 3
) {
    val loadingAlpha by animateFloatAsState(targetValue = if (loading) 1f else 0f)

    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(modifier = modifierBox, contentAlignment = Alignment.Center) {
            Button(
                onClick = { if (!loading) onClick() },
                interactionSource = interactionSource,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = colorResource(id = R.color.text_primary),
                    disabledBackgroundColor = Color.Transparent,
                    disabledContentColor = colorResource(id = R.color.text_tertiary)
                ),
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                ),
                contentPadding = PaddingValues(0.dp, 14.dp, 0.dp, 14.dp)
            ) {
                Text(
                    text = if (loading) "" else text,
                    modifier = Modifier,
                    style = ButtonRegular,
                    textAlign = TextAlign.Center
                )
            }
            DotsLoadingIndicator(
                animating = loading,
                modifier = Modifier.graphicsLayer { alpha = loadingAlpha },
                animationSpecs = FadeAnimationSpecs(itemCount = loadingItemsCount),
                color = colorResource(id = R.color.text_white),
                size = size
            )
        }
    }
}