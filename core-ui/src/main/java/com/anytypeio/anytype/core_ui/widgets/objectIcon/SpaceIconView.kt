package com.anytypeio.anytype.core_ui.widgets.objectIcon

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.toBitmap
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.res
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import com.anytypeio.anytype.presentation.wallpaper.getWallpaperColor
import timber.log.Timber

@Composable
fun SpaceIconView(
    modifier: Modifier = Modifier,
    mainSize: Dp = 96.dp,
    icon: SpaceIconView,
    backgroundColor: MutableState<Color>? = null,
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
            val painter = rememberAsyncImagePainter(model = icon.url)
            val state by painter.state.collectAsState()
            // compute once per successful result
            LaunchedEffect(state) {
                val success = state as? AsyncImagePainter.State.Success ?: return@LaunchedEffect
                val bmp = success.result.image.toBitmap() // handles most drawables
                try {
                    val avgArgb = bmp.averageColor1x1()
                    backgroundColor?.value = Color(avgArgb)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to compute average color")
                }
            }

            Image(
                painter = rememberAsyncImagePainter(model = icon.url),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = clickableModifier
                    .size(mainSize)
                    .clip(shape = CircleShape)
            )
        }

        is SpaceIconView.DataSpace.Image -> {
            val painter = rememberAsyncImagePainter(model = icon.url)
            val state by painter.state.collectAsState()
            // compute once per successful result
            LaunchedEffect(state) {
                val success = state as? AsyncImagePainter.State.Success ?: return@LaunchedEffect
                val image = success.result.image
                val bmp = image.toBitmap() // handles most drawables
                try {
                    val avgArgb = bmp.averageColor1x1()
                    backgroundColor?.value = Color(avgArgb)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to compute average color")
                }
            }

            Image(
                painter = rememberAsyncImagePainter(model = icon.url),
                contentDescription = "Custom image space icon",
                contentScale = ContentScale.Crop,
                modifier = clickableModifier
                    .size(mainSize)
                    .clip(RoundedCornerShape(radius))
            )
        }

        is SpaceIconView.ChatSpace.Placeholder -> {
            val color = icon.color.res()
            Box(
                modifier = clickableModifier
                    .size(mainSize)
                    .background(
                        color = color,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = icon
                        .name
                        .ifEmpty { stringResource(id = R.string.u) }
                        .take(1)
                        .uppercase(),
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.text_label_inversion),
                )
            }
        }

        is SpaceIconView.DataSpace.Placeholder -> {
            val color = icon.color.res()
            Box(
                modifier = clickableModifier
                    .size(mainSize)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(radius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = icon
                        .name
                        .ifEmpty { stringResource(id = R.string.u) }
                        .take(1)
                        .uppercase(),
                    textAlign = TextAlign.Center,
                    fontSize = fontSize,
                    color = colorResource(id = R.color.text_label_inversion),
                )
            }
        }

        SpaceIconView.Loading -> {

        }
    }
}

@ColorInt
fun Bitmap.averageColor1x1(): Int {
    if (isRecycled) error("Bitmap is recycled")

    // Ensure software bitmap if source is HARDWARE
    val sw = if (config == Bitmap.Config.HARDWARE) {
        copy(Bitmap.Config.ARGB_8888, false)
            ?: error("Failed to copy HARDWARE bitmap to software")
    } else {
        this
    }

    try {
        val tiny = Bitmap.createScaledBitmap(sw, 1, 1, /* filter = */ true)
        val c = tiny.getPixel(0, 0)
        tiny.recycle()
        return c
    } finally {
        if (sw !== this && !sw.isRecycled) sw.recycle()
    }
}

/**
 * Computes the background color for a space item based on priority:
 * 1. Wallpaper color (if available)
 * 2. Computed average color from image icon
 * 3. Placeholder icon color
 * 4. Default fallback color
 *
 * @param icon The SpaceIconView to compute background for
 * @param wallpaper Optional wallpaper configuration
 * @param backgroundColor State containing computed background color from image
 */
fun computeSpaceBackgroundColor(
    icon: SpaceIconView,
    wallpaper: Wallpaper? = null,
    backgroundColor: MutableState<Color>? = null
): Color {

    // First priority: Use wallpaper color if available
    if (wallpaper != null) {
        val wallpaperColor = getWallpaperColor(wallpaper)
        if (wallpaperColor != null) {
            return try {
                Color(android.graphics.Color.parseColor(wallpaperColor.hex))
            } catch (e: IllegalArgumentException) {
                // Handle invalid color format
                Timber.w(e, "Invalid wallpaper color format: ${wallpaperColor.hex}")
                Color(android.graphics.Color.parseColor(DEFAULT_SPACE_BACKGROUND_COLOR))
            }
        }
    }

    // Second priority: Use computed average color from image icon
    backgroundColor?.value?.let {
        if (it != Color.Transparent) return it
    }

    // Third priority: Use icon color for placeholders
    val spaceIconColor = getSpaceIconColor(icon)
    if (spaceIconColor != null) {
        return Color(spaceIconColor.resInt())
    }

    // Final fallback
    return Color(android.graphics.Color.parseColor(DEFAULT_SPACE_BACKGROUND_COLOR))
}

private fun getSpaceIconColor(icon: SpaceIconView): SystemColor? {
    return when (icon) {
        is SpaceIconView.ChatSpace.Placeholder -> icon.color
        is SpaceIconView.DataSpace.Placeholder -> icon.color
        else -> null
    }
}

private fun SystemColor.resInt(): Int {
    return when (this) {
        SystemColor.YELLOW -> R.color.palette_system_yellow
        SystemColor.AMBER -> R.color.palette_system_amber_100
        SystemColor.RED -> R.color.palette_system_red
        SystemColor.PINK -> R.color.palette_system_pink
        SystemColor.PURPLE -> R.color.palette_system_purple
        SystemColor.BLUE -> R.color.palette_system_blue
        SystemColor.SKY -> R.color.palette_system_sky
        SystemColor.TEAL -> R.color.palette_system_teal
        SystemColor.GREEN -> R.color.palette_system_green
    }
}

private val DEFAULT_SPACE_BACKGROUND_COLOR = WallpaperColor.ICE.hex

@DefaultPreviews
@Composable
private fun SpaceIconViewPreview() {
    SpaceIconView(
        icon = SpaceIconView.ChatSpace.Placeholder(
            name = "U"
        ),
        onSpaceIconClick = {},
        backgroundColor = Color.Gray.let { mutableStateOf(it) }
    )
}