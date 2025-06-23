package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun EmojiIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Emoji,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp,
    imageMultiplier: Float,
    backgroundColor: Int = R.color.shape_tertiary
) {
    val (containerModifier, iconModifier) = if (backgroundSize <= iconWithoutBackgroundMaxSize) {
        modifier.size(backgroundSize) to Modifier.size(backgroundSize)
    } else {
        modifier
            .size(backgroundSize)
            .background(
                color = colorResource(backgroundColor),
                shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
            ) to Modifier.size(
            width = backgroundSize * imageMultiplier,
            height = backgroundSize * imageMultiplier
        )
    }

    val emoji = Emojifier.safeUri(icon.unicode)

    if (emoji != Emojifier.Config.EMPTY_URI) {
        Box(
            modifier = containerModifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(emoji),
                contentDescription = "Emoji object icon",
                modifier = iconModifier
            )
        }
    } else {
        TypeIconView(
            modifier = modifier,
            icon = icon.fallback,
            backgroundSize = backgroundSize,
            imageMultiplier = imageMultiplier,
            iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize
        )
    }
}