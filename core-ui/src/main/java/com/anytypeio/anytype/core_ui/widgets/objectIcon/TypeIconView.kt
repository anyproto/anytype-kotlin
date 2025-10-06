package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.anytypeio.anytype.presentation.objects.ObjectIcon.TypeIcon.Deleted.DEFAULT_DELETED_ICON
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor

@Composable
fun TypeIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.TypeIcon,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp,
    backgroundColor: Int = R.color.shape_tertiary
) {
    when (icon) {
        is ObjectIcon.TypeIcon.Default -> {
            val (imageVector, tint) = getDefaultIconAndTint(icon)
            IconBoxView(
                boxModifier = modifier.size(backgroundSize),
                imageModifier = Modifier.size(backgroundSize),
                imageVector = imageVector,
                contentDescription = "Type icon",
                tint = tint
            )
        }

        ObjectIcon.TypeIcon.Deleted -> {
            val (boxModifier, imageModifier) = createIconModifiers(
                modifier = modifier,
                backgroundSize = backgroundSize,
                backgroundColor = backgroundColor,
                condition = backgroundSize > iconWithoutBackgroundMaxSize
            )
            DeletedTypeIconView(
                boxModifier = modifier.size(backgroundSize),
                imageModifier = Modifier.size(backgroundSize),
            )
        }

        is ObjectIcon.TypeIcon.Emoji -> {
            val emoji = Emojifier.safeUri(icon.unicode)
            if (emoji != Emojifier.Config.EMPTY_URI) {
                EmojiIconView(
                    modifier = modifier,
                    icon = ObjectIcon.Basic.Emoji(unicode = icon.unicode),
                    backgroundSize = backgroundSize,
                    iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize
                )
            } else {
                val (imageVector, tint) = getDefaultIconAndTint(icon)
                val (boxModifier, imageModifier) = createIconModifiers(
                    modifier = modifier,
                    backgroundSize = backgroundSize,
                    backgroundColor = backgroundColor,
                    condition = backgroundSize > iconWithoutBackgroundMaxSize
                )
                IconBoxView(
                    boxModifier = boxModifier,
                    imageModifier = imageModifier,
                    imageVector = imageVector,
                    contentDescription = "Type icon",
                    tint = tint
                )
            }
        }

        is ObjectIcon.TypeIcon.Fallback -> {
            val tint = colorResource(id = CustomIconColor.Transparent.colorRes())
            val imageVector =
                CustomIcons.getImageVector(icon.rawValue) ?: CustomIcons.CiExtensionPuzzle
            val (boxModifier, imageModifier) = createIconModifiers(
                modifier = modifier,
                backgroundSize = backgroundSize,
                backgroundColor = backgroundColor,
                condition = backgroundSize > iconWithoutBackgroundMaxSize
            )
            IconBoxView(
                boxModifier = boxModifier,
                imageModifier = imageModifier,
                imageVector = imageVector,
                contentDescription = "Type fallback icon",
                tint = tint
            )
        }
    }
}

/**
 * Helper composable to render an icon inside a Box with centered content.
 */
@Composable
fun IconBoxView(
    boxModifier: Modifier,
    imageModifier: Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color
) {
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = imageModifier,
            imageVector = imageVector,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

/**
 * Creates a pair of modifiers:
 * - [boxModifier] applies a background if [condition] is true,
 * - [imageModifier] sets the icon's size based on [backgroundSize].
 */
@Composable
private fun createIconModifiers(
    modifier: Modifier,
    backgroundSize: Dp,
    backgroundColor: Int,
    condition: Boolean
): Pair<Modifier, Modifier> {
    val baseModifier = modifier.size(backgroundSize)
    val boxModifier = if (condition) {
        baseModifier.background(
            color = colorResource(backgroundColor),
            shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
        )
    } else {
        baseModifier
    }

    return boxModifier to if (condition) {
        Modifier.size(getTypeIconDefaultParams(backgroundSize).dp)
    } else {
        Modifier.size(backgroundSize)
    }
}

/**
 * Returns the icon image vector and tint color for default/emoji icon types.
 */
@Composable
private fun getDefaultIconAndTint(icon: ObjectIcon.TypeIcon): Pair<ImageVector, Color> {
    val (rawValue, customIconColor) = when (icon) {
        is ObjectIcon.TypeIcon.Default -> icon.rawValue to icon.color
        ObjectIcon.TypeIcon.Deleted -> DEFAULT_DELETED_ICON to CustomIconColor.DEFAULT
        is ObjectIcon.TypeIcon.Emoji -> icon.rawValue to icon.color
        is ObjectIcon.TypeIcon.Fallback -> icon.rawValue to CustomIconColor.DEFAULT
    }
    var tint = colorResource(id = customIconColor.colorRes())
    val imageVector = CustomIcons.getImageVector(rawValue)
        ?: CustomIcons.CiExtensionPuzzle.also {
            tint = colorResource(id = CustomIconColor.DEFAULT.colorRes())
        }
    return imageVector to tint
}

@Composable
fun DeletedTypeIconView(
    boxModifier: Modifier,
    imageModifier: Modifier,
) {
    val tint = colorResource(id = CustomIconColor.DEFAULT.colorRes())
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            imageVector = CustomIcons.CiExtensionPuzzle,
            contentDescription = "Deleted type icon",
            modifier = imageModifier,
            colorFilter = ColorFilter.tint(tint)
        )
    }
}

/**
 * Returns the default icon size (in Int) based on the container's size.
 */
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