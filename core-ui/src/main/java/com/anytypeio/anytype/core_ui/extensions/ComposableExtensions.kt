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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.anytypeio.anytype.core_models.DayOfWeekCustom
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R

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
