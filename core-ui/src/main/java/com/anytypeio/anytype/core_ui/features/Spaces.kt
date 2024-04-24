package com.anytypeio.anytype.core_ui.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun SpaceIconView(
    modifier: Modifier = Modifier,
    mainSize: Dp = 96.dp,
    gradientSize: Dp = 64.dp,
    gradientBackground: Color = colorResource(id = R.color.default_gradient_background),
    gradientCornerRadius: Dp = 8.dp,
    icon: SpaceIconView,
    onSpaceIconClick: () -> Unit,
) {
    when (icon) {
        is SpaceIconView.Image -> {
            Image(
                painter = rememberAsyncImagePainter(
                    model = icon.url,
                    error = painterResource(id = R.drawable.ic_home_widget_space)
                ),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(4.dp))
                    .noRippleClickable {
                        onSpaceIconClick.invoke()
                    }
            )
        }
        is SpaceIconView.Gradient -> {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color(icon.from.toColorInt()),
                    Color(icon.to.toColorInt())
                )
            )
            Box(
                modifier = modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(gradientCornerRadius))
                    .background(color = gradientBackground)
                    .noRippleClickable { onSpaceIconClick.invoke() }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(gradientSize)
                        .clip(CircleShape)
                        .background(gradient)
                )
            }

        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_space),
                contentDescription = "Placeholder space icon",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(4.dp))
                    .noRippleClickable { onSpaceIconClick.invoke() }
            )
        }
    }
}