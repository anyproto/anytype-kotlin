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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.core_ui.widgets.imageAsset
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@Composable
fun EmojiIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Emoji,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp = 20.dp,
    imageMultiplier: Float = 0.625f,
    backgroundColor: Int = R.color.shape_tertiary
) {
    val (containerModifier, iconModifier) = if (backgroundSize > iconWithoutBackgroundMaxSize) {
        modifier
            .size(backgroundSize)
            .background(
                color = colorResource(backgroundColor),
                shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
            ) to Modifier.size(
            width = backgroundSize * imageMultiplier,
            height = backgroundSize * imageMultiplier
        )
    } else {
        modifier.size(backgroundSize) to Modifier
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {

        val emoji = Emojifier.safeUri(icon.unicode)
        if (emoji != Emojifier.Config.EMPTY_URI) {
            Image(
                painter = rememberAsyncImagePainter(emoji),
                contentDescription = "Icon from URI",
                modifier = iconModifier
            )
        } else {
            val imageAsset = imageAsset(icon.emptyState)
            Image(
                painter = painterResource(id = imageAsset),
                contentDescription = "Empty Object Icon",
                modifier = iconModifier
            )

        }
    }
}

@DefaultPreviews
@Composable
fun Emoji20ObjectIconViewPreview() {
    EmojiIconView(
        icon = ObjectIcon.Basic.Emoji("😀", ObjectIcon.Empty.Page),
        backgroundSize = 20.dp
    )
}