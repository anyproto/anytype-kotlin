package com.anytypeio.anytype.core_ui.extensions

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_models.DayOfWeekCustom
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient

@Composable
fun dark(
    color: ThemeColor,
): Color {
    return when (color) {
        ThemeColor.RED -> colorResource(id = R.color.palette_dark_red)
        ThemeColor.ORANGE -> colorResource(id = R.color.palette_dark_orange)
        ThemeColor.YELLOW -> colorResource(id = R.color.palette_dark_yellow)
        ThemeColor.TEAL -> colorResource(id = R.color.palette_dark_teal)
        ThemeColor.BLUE -> colorResource(id = R.color.palette_dark_blue)
        ThemeColor.PURPLE -> colorResource(id = R.color.palette_dark_purple)
        ThemeColor.PINK -> colorResource(id = R.color.palette_dark_pink)
        ThemeColor.ICE -> colorResource(id = R.color.palette_dark_ice)
        ThemeColor.LIME -> colorResource(id = R.color.palette_dark_lime)
        ThemeColor.GREY -> colorResource(id = R.color.palette_dark_grey)
        ThemeColor.DEFAULT -> colorResource(id = R.color.palette_dark_default)
    }
}

@Composable
fun dark(code: String): Color {
    val colorTheme = ThemeColor.entries.find { it.code == code } ?: ThemeColor.DEFAULT
    return dark(colorTheme)
}

@Composable
fun light(
    color: ThemeColor
) = when (color) {
    ThemeColor.RED -> colorResource(id = R.color.palette_light_red)
    ThemeColor.ORANGE -> colorResource(id = R.color.palette_light_orange)
    ThemeColor.YELLOW -> colorResource(id = R.color.palette_light_yellow)
    ThemeColor.TEAL -> colorResource(id = R.color.palette_light_teal)
    ThemeColor.BLUE -> colorResource(id = R.color.palette_light_blue)
    ThemeColor.PURPLE -> colorResource(id = R.color.palette_light_purple)
    ThemeColor.PINK -> colorResource(id = R.color.palette_light_pink)
    ThemeColor.ICE -> colorResource(id = R.color.palette_light_ice)
    ThemeColor.LIME -> colorResource(id = R.color.palette_light_lime)
    ThemeColor.GREY -> colorResource(id = R.color.palette_light_grey)
    ThemeColor.DEFAULT -> colorResource(id = R.color.palette_light_default)
}

@Composable
fun light(code: String): Color {
    val colorTheme = ThemeColor.entries.find { it.code == code } ?: ThemeColor.DEFAULT
    return light(colorTheme)
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bouncingClickable(
    enabled: Boolean = true,
    pressScaleFactor: Float = 0.97f,
    pressAlphaFactor: Float = 0.7f,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animationTransition = updateTransition(isPressed, label = "BouncingClickableTransition")
    val scaleFactor by animationTransition.animateFloat(
        targetValueByState = { pressed -> if (pressed) pressScaleFactor else 1f },
        label = "BouncingClickableScaleFactorTransition",
    )
    val opacity by animationTransition.animateFloat(
        targetValueByState = { pressed -> if (pressed) pressAlphaFactor else 1f },
        label = "BouncingClickableAlphaTransition",
    )

    this
        .graphicsLayer {
            scaleX = scaleFactor
            scaleY = scaleFactor
            alpha = opacity
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
        )
}

fun <T> SnapshotStateList<T>.swapList(newList: List<T>) {
    clear()
    addAll(newList)
}

@Composable
fun getLocalizedDayName(dayOfWeek: DayOfWeekCustom): String {
    return when (dayOfWeek) {
        DayOfWeekCustom.MONDAY -> stringResource(id = R.string.day_of_week_monday)
        DayOfWeekCustom.TUESDAY -> stringResource(id = R.string.day_of_week_tuesday)
        DayOfWeekCustom.WEDNESDAY -> stringResource(id = R.string.day_of_week_wednesday)
        DayOfWeekCustom.THURSDAY -> stringResource(id = R.string.day_of_week_thursday)
        DayOfWeekCustom.FRIDAY -> stringResource(id = R.string.day_of_week_friday)
        DayOfWeekCustom.SATURDAY -> stringResource(id = R.string.day_of_week_saturday)
        DayOfWeekCustom.SUNDAY -> stringResource(id = R.string.day_of_week_sunday)
    }
}

@Composable
fun RelativeDate.getPrettyName(): String {
    return when (this) {
        is RelativeDate.Today -> stringResource(id = R.string.today)
        is RelativeDate.Tomorrow -> stringResource(id = R.string.tomorrow)
        is RelativeDate.Yesterday -> stringResource(id = R.string.yesterday)
        is RelativeDate.Other -> this.formattedDate
        RelativeDate.Empty -> ""
    }
}

@Composable
fun SystemColor.res(): Color {
    val resInt = resInt()
    return colorResource(id = resInt)
}

fun SystemColor.resInt(): Int {
    return when (this) {
        SystemColor.GRAY -> R.color.palette_system_gray
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

fun SystemColor.resLightInt(): Int {
    return when (this) {
        SystemColor.GRAY -> R.color.palette_light_grey
        SystemColor.YELLOW -> R.color.palette_light_yellow
        SystemColor.AMBER -> R.color.palette_light_orange
        SystemColor.RED -> R.color.palette_light_red
        SystemColor.PINK -> R.color.palette_light_pink
        SystemColor.PURPLE -> R.color.palette_light_purple
        SystemColor.BLUE -> R.color.palette_light_blue
        SystemColor.SKY -> R.color.palette_light_ice
        SystemColor.TEAL -> R.color.palette_light_teal
        SystemColor.GREEN -> R.color.palette_light_lime
    }
}

/**
 * Gets gradient colors for wallpaper gradients using actual start/end colors
 */
@Composable
fun getWallpaperGradientByCode(gradientCode: String): GradientColors? {
    return when (gradientCode) {
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

/**
 * Data class representing gradient colors for wallpaper backgrounds
 */
data class GradientColors(
    val startColor: Color,
    val endColor: Color
) {
    fun toBrush(): Brush = Brush.linearGradient(
        colors = listOf(startColor, endColor),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY)
    )
}
