package com.anytypeio.anytype.core_ui.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun SpaceIconView(
    modifier: Modifier = Modifier,
    mainSize: Dp = 96.dp,
    icon: SpaceIconView,
    onSpaceIconClick: () -> Unit,
) {
    val radius = when(mainSize) {
        20.dp -> 4.dp
        28.dp -> 4.dp
        40.dp -> 5.dp
        48.dp -> 6.dp
        64.dp -> 8.dp
        96.dp -> 12.dp
        else -> 6.dp
    }

    val fontSize = when(mainSize) {
        20.dp -> 16.sp
        28.dp -> 16.sp
        40.dp -> 24.sp
        48.dp -> 28.sp
        64.dp -> 40.sp
        96.dp -> 65.sp
        else -> 28.sp
    }

    when (icon) {
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(model = icon.url),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(radius))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
            )
        }
        is SpaceIconView.Placeholder -> {
            val color = when (icon.color) {
                SystemColor.YELLOW -> colorResource(id = R.color.palette_system_yellow)
                SystemColor.AMBER -> colorResource(id = R.color.palette_system_amber_100)
                SystemColor.RED -> colorResource(id = R.color.palette_system_red)
                SystemColor.PINK -> colorResource(id = R.color.palette_system_pink)
                SystemColor.PURPLE -> colorResource(id = R.color.palette_system_purple)
                SystemColor.BLUE -> colorResource(id = R.color.palette_system_blue)
                SystemColor.SKY -> colorResource(id = R.color.palette_system_sky)
                SystemColor.TEAL -> colorResource(id = R.color.palette_system_teal)
                SystemColor.GREEN -> colorResource(id = R.color.palette_system_green)
            }
            Box(
                modifier = modifier
                    .size(mainSize)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.5f),
                                color
                            )
                        ),
                        shape = RoundedCornerShape(radius)
                    )
                    .clip(RoundedCornerShape(radius))
                    .noRippleClickable { onSpaceIconClick.invoke() }
            ) {
                Text(
                    modifier = Modifier.align(
                        Alignment.Center
                    ),
                    text = icon
                        .name
                        .ifEmpty { stringResource(id = R.string.u) }
                        .take(1)
                        .uppercase(),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight(600),
                        color = colorResource(id = R.color.text_white),
                    )
                )
            }
        }
        else -> {
            // Draw nothing
        }
    }
}