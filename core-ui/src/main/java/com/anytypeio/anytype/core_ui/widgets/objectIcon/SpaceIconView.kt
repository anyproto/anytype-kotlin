package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.res
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun SpaceIconView(
    modifier: Modifier = Modifier,
    mainSize: Dp = 96.dp,
    icon: SpaceIconView,
    onSpaceIconClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onSpaceIconClick != null) {
        modifier.noRippleThrottledClickable { onSpaceIconClick() }
    } else {
        modifier
    }
    val radius = when (mainSize) {
        20.dp -> 2.dp
        28.dp, 32.dp -> 4.dp
        40.dp -> 5.dp
        48.dp -> 6.dp
        64.dp -> 10.dp
        96.dp -> 12.dp
        else -> 6.dp
    }

    val fontSize = when (mainSize) {
        20.dp -> 13.sp
        28.dp, 32.dp -> 20.sp
        40.dp -> 24.sp
        48.dp -> 28.sp
        64.dp -> 40.sp
        96.dp -> 65.sp
        else -> 28.sp
    }

    when (icon) {
        is SpaceIconView.ChatSpace.Image -> {
            SpaceImage(
                url = icon.url,
                shape = CircleShape,
                mainSize = mainSize,
                modifier = clickableModifier
            )
        }

        is SpaceIconView.DataSpace.Image -> {
            SpaceImage(
                url = icon.url,
                shape = RoundedCornerShape(radius),
                mainSize = mainSize,
                modifier = clickableModifier
            )
        }

        is SpaceIconView.ChatSpace.Placeholder -> {
            SpacePlaceholder(
                name = icon.name,
                color = icon.color.res(),
                shape = CircleShape,
                mainSize = mainSize,
                fontSize = fontSize,
                modifier = clickableModifier
            )
        }

        is SpaceIconView.DataSpace.Placeholder -> {
            SpacePlaceholder(
                name = icon.name,
                color = icon.color.res(),
                shape = RoundedCornerShape(radius),
                mainSize = mainSize,
                fontSize = fontSize,
                modifier = clickableModifier
            )
        }

        SpaceIconView.Loading -> {
            //do nothing
        }
    }
}

@Composable
private fun SpaceImage(
    url: String,
    shape: Shape,
    mainSize: Dp,
    modifier: Modifier
) {
    val painter = rememberAsyncImagePainter(model = url)

    Image(
        painter = painter,
        contentDescription = "Custom image space icon",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(mainSize)
            .clip(shape)
    )
}

@Composable
private fun SpacePlaceholder(
    name: String,
    color: Color,
    shape: Shape,
    mainSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .size(mainSize)
            .background(color = color, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = name
                .ifEmpty { stringResource(id = R.string.u) }
                .take(1)
                .uppercase(),
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.text_label_inversion)
        )
    }
}


@DefaultPreviews
@Composable
private fun SpaceIconViewPreview() {
    SpaceIconView(
        icon = SpaceIconView.ChatSpace.Placeholder(
            name = "U"
        ),
        onSpaceIconClick = {}
    )
}