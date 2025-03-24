package com.anytypeio.anytype.core_ui.views.animations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.anytypeio.anytype.core_ui.R

@Composable
fun LoadingIndicator(
    containerModifier: Modifier = Modifier,
    containerSize: Dp,
    colorStart: Color = colorResource(id = R.color.glyph_active),
    colorEnd: Color = Color.Transparent,
    withCircleBackground: Boolean = true
) {
    //todo next PR
}