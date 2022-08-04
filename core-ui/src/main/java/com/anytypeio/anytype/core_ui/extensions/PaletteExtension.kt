package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_models.ThemeColor

@ColorInt
fun Resources.text(
    color: ThemeColor,
    default: Int = getColor(R.color.text_primary, null)
) = dark(
    color = color,
    default = default
)

@ColorInt
fun Resources.dark(
    color: ThemeColor,
    @ColorInt
    default: Int? = null
): Int = when (color) {
    // TODO pass Theme
    ThemeColor.DEFAULT -> default ?: getColor(R.color.palette_dark_default, null)
    ThemeColor.GREY -> getColor(R.color.palette_dark_grey, null)
    ThemeColor.YELLOW -> getColor(R.color.palette_dark_yellow, null)
    ThemeColor.ORANGE -> getColor(R.color.palette_dark_orange, null)
    ThemeColor.RED -> getColor(R.color.palette_dark_red, null)
    ThemeColor.PINK -> getColor(R.color.palette_dark_pink, null)
    ThemeColor.PURPLE -> getColor(R.color.palette_dark_purple, null)
    ThemeColor.BLUE -> getColor(R.color.palette_dark_blue, null)
    ThemeColor.ICE -> getColor(R.color.palette_dark_ice, null)
    ThemeColor.TEAL -> getColor(R.color.palette_dark_teal, null)
    ThemeColor.LIME -> getColor(R.color.palette_dark_lime, null)
}

@ColorInt
fun Resources.light(
    color: ThemeColor,
    @ColorInt
    default: Int? = null
): Int = when (color) {
    ThemeColor.DEFAULT -> default ?: getColor(R.color.palette_light_default, null)
    ThemeColor.GREY -> getColor(R.color.palette_light_grey, null)
    ThemeColor.YELLOW -> getColor(R.color.palette_light_yellow, null)
    ThemeColor.ORANGE -> getColor(R.color.palette_light_orange, null)
    ThemeColor.RED -> getColor(R.color.palette_light_red, null)
    ThemeColor.PINK -> getColor(R.color.palette_light_pink, null)
    ThemeColor.PURPLE -> getColor(R.color.palette_light_purple, null)
    ThemeColor.BLUE -> getColor(R.color.palette_light_blue, null)
    ThemeColor.ICE -> getColor(R.color.palette_light_ice, null)
    ThemeColor.TEAL -> getColor(R.color.palette_light_teal, null)
    ThemeColor.LIME -> getColor(R.color.palette_light_lime, null)
}

@ColorInt
fun Resources.veryLight(
    color: ThemeColor,
    @ColorInt
    default: Int? = null
): Int = when (color) {
    ThemeColor.DEFAULT -> default ?: getColor(R.color.palette_very_light_default, null)
    ThemeColor.GREY -> getColor(R.color.palette_very_light_grey, null)
    ThemeColor.YELLOW -> getColor(R.color.palette_very_light_yellow, null)
    ThemeColor.ORANGE -> getColor(R.color.palette_very_light_orange, null)
    ThemeColor.RED -> getColor(R.color.palette_very_light_red, null)
    ThemeColor.PINK -> getColor(R.color.palette_very_light_pink, null)
    ThemeColor.PURPLE -> getColor(R.color.palette_very_light_purple, null)
    ThemeColor.BLUE -> getColor(R.color.palette_very_light_blue, null)
    ThemeColor.ICE -> getColor(R.color.palette_very_light_ice, null)
    ThemeColor.TEAL -> getColor(R.color.palette_very_light_teal, null)
    ThemeColor.LIME -> getColor(R.color.palette_very_light_lime, null)
}

fun TextView.setTextColor(color: ThemeColor, defaultColor: Int = R.color.text_primary) {
    val default = context.getColor(defaultColor)
    if (color != ThemeColor.DEFAULT) {
        setTextColor(resources.dark(color, default))
    } else {
        setTextColor(default)
    }
}

fun TextView.setTextColor(color: String, defaultColor: Int = R.color.text_primary) {
    val value = ThemeColor.values().find { value -> value.code == color }
    val default = context.getColor(defaultColor)
    if (value != null && value != ThemeColor.DEFAULT) {
        setTextColor(resources.dark(value, default))
    } else {
        setTextColor(default)
    }
}


/**
 * @param [color] color code, @see [ThemeColor]
 */
fun View.setBlockBackgroundColor(color: String?) {
    if (!color.isNullOrEmpty()) {
        val value = ThemeColor.values().find { value -> value.code == color }
        if (value != null && value != ThemeColor.DEFAULT) {
            setBackgroundColor(resources.veryLight(value, 0))
        } else {
            background = null
        }
    } else {
        background = null
    }
}

fun View.setBlockBackgroundColor(bg: ThemeColor) {
    if (bg != ThemeColor.DEFAULT) {
        setBackgroundColor(resources.veryLight(bg, 0))
    } else {
        background = null
    }
}

/**
 * @param [color] color code, @see [ThemeColor]
 */
fun View.setBlockBackgroundTintColor(
    color: String?,
    default: ThemeColor = ThemeColor.DEFAULT
) {
    if (background == null) return
    if (!color.isNullOrEmpty()) {
        val value = ThemeColor.values().find { value -> value.code == color }
        if (value != null && value != ThemeColor.DEFAULT) {
            DrawableCompat.setTint(
                background,
                resources.veryLight(value, 0)
            )
        } else {
            DrawableCompat.setTint(
                background,
                resources.veryLight(default, 0)
            )
        }
    } else {
        DrawableCompat.setTint(
            background,
            resources.veryLight(default, 0)
        )
    }
}

/**
 * @param [color] color code, @see [ThemeColor]
 */
fun View.setBlockBackgroundTintColor(
    color: ThemeColor,
    default: Int
) {
    DrawableCompat.setTint(
        background,
        resources.veryLight(color, default)
    )
}

/**
 * @param [color] color code, @see [ThemeColor]
 */
fun Context.resolveThemedTextColor(color: String?, defaultColor: Int): Int {
    val value = ThemeColor.values().find { value -> value.code == color }
    return if (value != null && value != ThemeColor.DEFAULT) {
        resources.text(value, defaultColor)
    } else {
        defaultColor
    }
}

fun Context.resolveThemedTextColor(color: ThemeColor, defaultColor: Int): Int {
    return if (color != ThemeColor.DEFAULT) {
        resources.text(color, defaultColor)
    } else {
        defaultColor
    }
}