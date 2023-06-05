package com.anytypeio.anytype.core_ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryActive
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryInactive
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryInactiveText
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryPressed
import com.anytypeio.anytype.core_ui.ColorButtonPrimaryText
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryActive
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorder
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryBorderPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryPressed
import com.anytypeio.anytype.core_ui.ColorButtonSecondaryText
import com.anytypeio.anytype.core_ui.R

@Composable
fun OnBoardingButtonPrimary(
    text: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
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

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
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
            Text(
                text = text,
                style = size.textStyle
            )
        }
    }
}

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

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            enabled = enabled,
            shape = RoundedCornerShape(size.cornerSize),
            border = BorderStroke(width = 1.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor
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
                style = size.textStyle.copy(
                    color = ColorButtonSecondaryText
                )
            )
        }
    }
}