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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.anytypeio.anytype.core_ui.common.AVG_COLOR_EXTRA
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.buildImageLoader
import com.anytypeio.anytype.core_ui.extensions.res
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.wallpaper.WallpaperColor
import timber.log.Timber

/**
 * Data class representing gradient colors for wallpaper backgrounds
 */
data class GradientColors(
    val startColor: Color,
    val endColor: Color
) {
    fun toBrush(): Brush = Brush.linearGradient(
        colors = listOf(startColor, endColor),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
    )
}

@Composable
fun SpaceIconView(
    modifier: Modifier = Modifier,
    mainSize: Dp = 96.dp,
    icon: SpaceIconView,
    backgroundColorState: MutableState<Color>? = null,
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
                modifier = clickableModifier,
                backgroundColor = backgroundColorState
            )
        }

        is SpaceIconView.DataSpace.Image -> {
            SpaceImage(
                url = icon.url,
                shape = RoundedCornerShape(radius),
                mainSize = mainSize,
                modifier = clickableModifier,
                backgroundColor = backgroundColorState
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

        SpaceIconView.Loading -> { /* no-op */ }
    }
}

@Composable
private fun SpaceImage(
    url: String,
    shape: Shape,
    mainSize: Dp,
    modifier: Modifier,
    backgroundColor: MutableState<Color>? = null
) {
    val context = LocalContext.current
    val spaceImageLoader = remember {
        buildImageLoader(
            context = context,
            compute = { bmp -> bmp.averageColor1x1() }
        )
    }
    val painter = rememberAsyncImagePainter(model = url, imageLoader = spaceImageLoader)
    val state by painter.state.collectAsState()

    // Compute once per successful result using the same painter instance
    LaunchedEffect(state) {
        val success = state as? AsyncImagePainter.State.Success ?: return@LaunchedEffect
        val key = success.result.memoryCacheKey
        val cache = spaceImageLoader.memoryCache
        val value = key?.let { cache?.get(it) }
        val cached = (value?.extras?.get(AVG_COLOR_EXTRA) as? Int)

        if (cached != null) {
            Timber.i("[AvgColor/UI] Using cached color key=%s value=#%08X", key, cached)
            backgroundColor?.value = Color(cached)
            return@LaunchedEffect
        }

        // Fallback—should be rare if interceptor stored it
        runCatching { success.result.image.toBitmap().averageColor1x1() }
            .onSuccess { avg ->
                backgroundColor?.value = Color(avg)
                Timber.i("[AvgColor/UI] Fallback computed=#%08X for key=%s", avg, key)
            }
            .onFailure { e ->
                Timber.w(e, "[AvgColor/UI] Fallback failed for key=%s", key)
            }
    }

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

@ColorInt
fun Bitmap.averageColor1x1(): Int {
    if (isRecycled) error("Bitmap is recycled")

    var sw: Bitmap? = null
    try {
        // Ensure software bitmap if source is HARDWARE
        sw = if (config == Bitmap.Config.HARDWARE) {
            copy(Bitmap.Config.ARGB_8888, false)
                ?: error("Failed to copy HARDWARE bitmap to software")
        } else {
            this
        }

        val tiny = Bitmap.createScaledBitmap(sw, 1, 1, /* filter = */ true)
        val c = tiny.getPixel(0, 0)
        tiny.recycle()
        return c
    } finally {
        if (sw !== this && sw != null && !sw.isRecycled) sw.recycle()
    }
}

/**
 * Sealed class representing different background types for space icons
 */
sealed class SpaceBackground {
    data class SolidColor(val color: Color) : SpaceBackground()
    data class Gradient(val brush: Brush) : SpaceBackground()
}

/**
 * Computes the background for a SpaceIconView with priority:
 * 1. Wallpaper (gradient or color if available)
 * 2. Computed average color from image icon
 * 3. Icon color for placeholders
 * 4. Default fallback color
 *
 * @param icon The SpaceIconView to compute background for
 * @param wallpaper Optional wallpaper configuration
 * @param backgroundColor State containing computed background color from image
 */
@Composable
fun computeSpaceBackground(
    icon: SpaceIconView,
    wallpaper: Wallpaper? = null,
    backgroundColor: MutableState<Color>? = null
): SpaceBackground {

    // First priority: Use wallpaper if available
    if (wallpaper != null) {
        when (wallpaper) {
            is Wallpaper.Gradient -> {
                val gradient = getWallpaperGradient(wallpaper)
                if (gradient != null) {
                    return SpaceBackground.Gradient(gradient.toBrush())
                }
            }
            is Wallpaper.Color -> {
                val wallpaperColor = WallpaperColor.entries.find { it.code == wallpaper.code }
                if (wallpaperColor != null) {
                    return try {
                        SpaceBackground.SolidColor(
                            Color(android.graphics.Color.parseColor(wallpaperColor.hex))
                        )
                    } catch (e: IllegalArgumentException) {
                        // Handle invalid color format
                        Timber.w(e, "Invalid wallpaper color format: ${wallpaperColor.hex}")
                        SpaceBackground.SolidColor(
                            Color(android.graphics.Color.parseColor(DEFAULT_SPACE_BACKGROUND_COLOR))
                        )
                    }
                }
            }
            is Wallpaper.Image -> {
                // For images, we can't extract a color, skip to next priority
            }
            is Wallpaper.Default -> {
                SpaceBackground.SolidColor(
                    Color(android.graphics.Color.parseColor(DEFAULT_SPACE_BACKGROUND_COLOR))
                )
            }
        }
    }

    // Second priority: Use computed average color from image icon
    backgroundColor?.value?.let {
        if (it != Color.Transparent) return SpaceBackground.SolidColor(it)
    }

    // Third priority: Use icon color for placeholders
    val iconColor = getSpaceIconColor(icon)
    if (iconColor != null) {
        return SpaceBackground.SolidColor(iconColor.res())
    }

    // Default fallback
    return SpaceBackground.SolidColor(Color(android.graphics.Color.parseColor(DEFAULT_SPACE_BACKGROUND_COLOR)))
}

private fun getSpaceIconColor(icon: SpaceIconView): SystemColor? {
    return when (icon) {
        is SpaceIconView.ChatSpace.Placeholder -> icon.color
        is SpaceIconView.DataSpace.Placeholder -> icon.color
        else -> null
    }
}

/**
 * Gets gradient colors for wallpaper gradients using actual start/end colors
 */
@Composable
private fun getWallpaperGradient(wallpaper: Wallpaper.Gradient): GradientColors? {
    return when (wallpaper.code) {
        CoverGradient.YELLOW -> GradientColors(
            startColor = colorResource(R.color.yellowStart),
            endColor = colorResource(R.color.yellowEnd)
        )
        CoverGradient.RED -> GradientColors(
            startColor = colorResource(R.color.redStart),
            endColor = colorResource(R.color.redEnd)
        )
        CoverGradient.BLUE -> GradientColors(
            startColor = colorResource(R.color.blueStart),
            endColor = colorResource(R.color.blueEnd)
        )
        CoverGradient.TEAL -> GradientColors(
            startColor = colorResource(R.color.tealStart),
            endColor = colorResource(R.color.tealEnd)
        )
        CoverGradient.PINK_ORANGE -> GradientColors(
            startColor = colorResource(R.color.pinkOrangeStart),
            endColor = colorResource(R.color.pinkOrangeEnd)
        )
        CoverGradient.BLUE_PINK -> GradientColors(
            startColor = colorResource(R.color.bluePinkStart),
            endColor = colorResource(R.color.bluePinkEnd)
        )
        CoverGradient.GREEN_ORANGE -> GradientColors(
            startColor = colorResource(R.color.greenOrangeStart),
            endColor = colorResource(R.color.greenOrangeEnd)
        )
        CoverGradient.SKY -> GradientColors(
            startColor = colorResource(R.color.skyStart),
            endColor = colorResource(R.color.skyEnd)
        )
        else -> null
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
        backgroundColorState = Color.Gray.let { mutableStateOf(it) }
    )
}