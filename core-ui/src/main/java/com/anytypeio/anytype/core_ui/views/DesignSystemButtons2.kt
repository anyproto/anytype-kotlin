package com.anytypeio.anytype.core_ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonUpgrade(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = BodyCalloutRegular
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor =
        if (isPressed.value) colorResource(id = R.color.button_pressed) else colorResource(
            id = R.color.glyph_selected
        )

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    indication = null
                )
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = text,
                style = style,
                color = colorResource(id = R.color.button_text)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonUpgradeBlack(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = BodyCalloutRegular
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = colorResource(id = R.color.black)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = text,
                style = style,
                color = colorResource(id = R.color.white)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonIncentiveSecond(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = UxSmallTextMedium
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val backgroundColor = Color(0x1C000000)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = RoundedCornerShape(100.dp))
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick,
                    indication = null
                )
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = text,
                style = style,
                color = colorResource(id = R.color.black)
            )
        }
    }
}

@Composable
@DefaultPreviews
fun MyButtonUpgrade() {
    ButtonUpgrade(
        onClick = {},
        text = "✦ Upgrade",
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(36.dp)
    )
}

@Composable
@DefaultPreviews
fun MyButtonIncentiveSecond() {
    ButtonIncentiveSecond(
        onClick = {},
        text = "Manage Spaces",
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(36.dp)
    )
}

@Composable
@DefaultPreviews
fun MyButtonUpgradeBlackPreview() {
    ButtonUpgradeBlack(
        onClick = {},
        text = "✦ Upgrade",
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(36.dp)
    )
}