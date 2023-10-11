package com.anytypeio.anytype.core_ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.toColorInt

@Composable
fun RadialGradientComposeView(
    modifier: Modifier,
    from: String,
    to: String,
    size: Dp
) {
    val gradient = Brush.radialGradient(
        colors = listOf(
            Color(from.toColorInt()),
            Color(to.toColorInt())
        )
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient)
    )
}