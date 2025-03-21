package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

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
            val fallback = icon.fallback

            val tint = colorResource(id = fallback.color.colorRes())

            val imageVector = CustomIcons.getImageVector(fallback.rawValue)
            val fallbackIcon = CustomIcons.getImageVector(fallback.fallback)

            Box(modifier = Modifier) {
                if (imageVector != null) {
                    Image(
                        modifier = iconModifier,
                        imageVector = imageVector,
                        contentDescription = "Object Type icon",
                        colorFilter = ColorFilter.tint(tint),
                    )
                } else {
                    if (fallbackIcon != null) {
                        Image(
                            modifier = iconModifier,
                            imageVector = fallbackIcon,
                            contentDescription = "Object Type icon",
                            colorFilter = ColorFilter.tint(tint),
                        )
                    }
                }
            }
        }
    }
}

@DefaultPreviews
@Composable
fun Emoji20ObjectIconViewPreview() {
    Column {
        Text("Proper emoji case:")
        EmojiIconView(
            icon = ObjectIcon.Basic.Emoji(
                "😀"
            ),
            backgroundSize = 20.dp
        )
        Text("Fallback on type icon:")
        EmojiIconView(
            icon = ObjectIcon.Basic.Emoji(
                "😀1111",
                fallback = ObjectIcon.TypeIcon.Default(
                    rawValue = CustomIcons.iconsMap.entries.first().key,
                    color = CustomIconColor.Teal
                )
            ),
            backgroundSize = 20.dp
        )
        Text("Fallback on default icon in case of invalid type icon:")
        EmojiIconView(
            icon = ObjectIcon.Basic.Emoji(
                "😀1111",
                fallback = ObjectIcon.TypeIcon.Default(
                    rawValue = "no-icon-like-that",
                    color = CustomIconColor.DEFAULT
                )
            ),
            backgroundSize = 20.dp
        )
    }
}