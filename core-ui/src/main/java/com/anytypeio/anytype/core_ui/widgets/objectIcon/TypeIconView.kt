package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.colorRes
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CiExtensionPuzzle
import com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons.CustomIcons
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

@Composable
fun TypeIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.TypeIcon,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp = 20.dp,
    backgroundColor: Int = R.color.shape_tertiary
) {

    when (icon) {
        is ObjectIcon.TypeIcon.Default -> {
            var tint = colorResource(id = icon.color.colorRes())
            val imageVector =
                CustomIcons.getImageVector(icon.rawValue) ?: CustomIcons.CiExtensionPuzzle.also {
                    tint = colorResource(id = CustomIconColor.DEFAULT.colorRes())
                }

            val (boxModifier, imageModifier) = if (backgroundSize >= 80.dp) {
                modifier
                    .size(backgroundSize)
                    .background(
                        color = colorResource(backgroundColor),
                        shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
                    ) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            } else {
                modifier.size(backgroundSize) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            }

            Box(
                modifier = boxModifier,
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    modifier = imageModifier,
                    imageVector = imageVector,
                    contentDescription = "Type icon",
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }

        ObjectIcon.TypeIcon.Deleted -> {

            val (boxModifier, imageModifier) = if (backgroundSize >= 80.dp) {
                modifier
                    .size(backgroundSize)
                    .background(
                        color = colorResource(backgroundColor),
                        shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
                    ) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            } else {
                modifier.size(backgroundSize) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            }

            DeletedTypeIconView(
                boxModifier = boxModifier,
                imageModifier = imageModifier,
            )
        }

        is ObjectIcon.TypeIcon.Emoji -> {
            val emoji = Emojifier.safeUri(icon.unicode)
            if (emoji != Emojifier.Config.EMPTY_URI) {
                EmojiIconView(
                    icon = ObjectIcon.Basic.Emoji(
                        unicode = icon.unicode,
                    ),
                    backgroundSize = backgroundSize
                )
            } else {
                var tint = colorResource(id = icon.color.colorRes())
                val imageVector =
                    CustomIcons.getImageVector(icon.rawValue) ?: CustomIcons.CiExtensionPuzzle.also {
                        tint = colorResource(id = CustomIconColor.DEFAULT.colorRes())
                    }

                val (boxModifier, imageModifier) = if (backgroundSize >= 80.dp) {
                    modifier
                        .size(backgroundSize)
                        .background(
                            color = colorResource(backgroundColor),
                            shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
                        ) to Modifier.size(
                        size = getTypeIconDefaultParams(backgroundSize).dp,
                    )
                } else {
                    modifier.size(backgroundSize) to Modifier.size(
                        size = getTypeIconDefaultParams(backgroundSize).dp,
                    )
                }

                Box(
                    modifier = boxModifier,
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Image(
                        modifier = imageModifier,
                        imageVector = imageVector,
                        contentDescription = "Type icon",
                        colorFilter = ColorFilter.tint(tint),
                    )
                }
            }
        }

        is ObjectIcon.TypeIcon.Fallback -> {
            val tint = colorResource(id = CustomIconColor.Transparent.colorRes())
            val imageVector = CustomIcons.getImageVector(icon.rawValue) ?: CustomIcons.CiExtensionPuzzle

            val (boxModifier, imageModifier) = if (backgroundSize > iconWithoutBackgroundMaxSize) {
                modifier
                    .size(backgroundSize)
                    .background(
                        color = colorResource(backgroundColor),
                        shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
                    ) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            } else {
                modifier.size(backgroundSize) to Modifier.size(
                    size = getTypeIconDefaultParams(backgroundSize).dp,
                )
            }

            Box(
                modifier = boxModifier,
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    modifier = imageModifier,
                    imageVector = imageVector,
                    contentDescription = "Type fallback icon",
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }
    }
}

@Composable
fun DeletedTypeIconView(
    boxModifier: Modifier,
    imageModifier: Modifier,
) {
    val tint = colorResource(id = CustomIconColor.DEFAULT.colorRes())
    Box(
        modifier = boxModifier, contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Image(
            imageVector = CustomIcons.CiExtensionPuzzle,
            contentDescription = "Deleted type icon",
            modifier = imageModifier,
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

//get type default icon - icon size
private fun getTypeIconDefaultParams(containerSize: Dp): Int {
    return when (containerSize) {
        in 0.dp..16.dp -> 16
        in 17.dp..18.dp -> 18
        in 19.dp..39.dp -> 20
        in 40.dp..47.dp -> 24
        in 48.dp..63.dp -> 30
        in 64.dp..79.dp -> 36
        in 80.dp..95.dp -> 48
        in 96.dp..111.dp -> 52
        else -> 64
    }
}