package com.anytypeio.anytype.core_ui.extensions

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R

@Composable
fun dark(
    color: ThemeColor,
): Color {
    return when (color) {
        ThemeColor.RED -> colorResource(id = R.color.palette_dark_red)
        ThemeColor.ORANGE -> colorResource(id = R.color.palette_dark_orange)
        ThemeColor.YELLOW -> colorResource(id = R.color.palette_dark_yellow)
        ThemeColor.TEAL -> colorResource(id = R.color.palette_dark_teal)
        ThemeColor.BLUE -> colorResource(id = R.color.palette_dark_blue)
        ThemeColor.PURPLE -> colorResource(id = R.color.palette_dark_purple)
        ThemeColor.PINK -> colorResource(id = R.color.palette_dark_pink)
        ThemeColor.ICE -> colorResource(id = R.color.palette_dark_ice)
        ThemeColor.LIME -> colorResource(id = R.color.palette_dark_lime)
        ThemeColor.GREY -> colorResource(id = R.color.palette_dark_grey)
        ThemeColor.DEFAULT -> colorResource(id = R.color.palette_dark_default)
    }
}

@Composable
fun light(
    color: ThemeColor
) = when (color) {
    ThemeColor.RED -> colorResource(id = R.color.palette_light_red)
    ThemeColor.ORANGE -> colorResource(id = R.color.palette_light_orange)
    ThemeColor.YELLOW -> colorResource(id = R.color.palette_light_yellow)
    ThemeColor.TEAL -> colorResource(id = R.color.palette_light_teal)
    ThemeColor.BLUE -> colorResource(id = R.color.palette_light_blue)
    ThemeColor.PURPLE -> colorResource(id = R.color.palette_light_purple)
    ThemeColor.PINK -> colorResource(id = R.color.palette_light_pink)
    ThemeColor.ICE -> colorResource(id = R.color.palette_light_ice)
    ThemeColor.LIME -> colorResource(id = R.color.palette_light_lime)
    ThemeColor.GREY -> colorResource(id = R.color.palette_light_grey)
    ThemeColor.DEFAULT -> colorResource(id = R.color.palette_light_default)
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bouncingClickable(
    enabled: Boolean = true,
    pressScaleFactor: Float = 0.97f,
    pressAlphaFactor: Float = 0.7f,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationTransition = updateTransition(isPressed, label = "BouncingClickableTransition")
    val scaleFactor by animationTransition.animateFloat(
        targetValueByState = { pressed -> if (pressed) pressScaleFactor else 1f },
        label = "BouncingClickableScaleFactorTransition",
    )
    val opacity by animationTransition.animateFloat(
        targetValueByState = { pressed -> if (pressed) pressAlphaFactor else 1f },
        label = "BouncingClickableAlphaTransition",
    )

    this
        .graphicsLayer {
            scaleX = scaleFactor
            scaleY = scaleFactor
            alpha = opacity
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
        )
}
