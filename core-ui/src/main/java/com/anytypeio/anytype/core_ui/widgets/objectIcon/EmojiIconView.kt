package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.EmojiUtils
import com.anytypeio.anytype.core_ui.widgets.contentSizeForBackground
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun EmojiIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Emoji,
    backgroundSize: Dp,
    emojiFontSize: Float = backgroundSize.value,
    iconWithoutBackgroundMaxSize: Dp,
    backgroundColor: Int = R.color.shape_tertiary
) {
    val (containerModifier, iconModifier) = if (backgroundSize <= iconWithoutBackgroundMaxSize) {
        modifier.size(backgroundSize) to Modifier.size(backgroundSize)
    } else {
        modifier
            .size(backgroundSize)
            .background(
                color = colorResource(backgroundColor),
                shape = if (icon.circleShape){
                    CircleShape
                } else {
                    RoundedCornerShape(size = cornerRadius(backgroundSize))
                }
            ) to Modifier.size(
            contentSizeForBackground(backgroundSize)
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
    } else if (EmojiUtils.isReady()) {
        // Fallback to EmojiCompat when emoji not found in Emojifier
        Box(
            modifier = containerModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = EmojiUtils.processSafe(icon.unicode).toString(),
                fontSize = emojiFontSize.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        TypeIconView(
            modifier = modifier,
            icon = icon.fallback,
            backgroundSize = backgroundSize,
            iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize
        )
    }
}