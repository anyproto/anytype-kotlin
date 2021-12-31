package com.anytypeio.anytype.core_ui.extensions

import android.content.res.Resources
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor

fun Resources.dark(color: ThemeColor, default: Int): Int = when (color) {
    ThemeColor.DEFAULT -> default
    ThemeColor.GREY -> getColor(R.color.palette_dark_grey, null)
    ThemeColor.YELLOW -> getColor(R.color.palette_dark_yellow, null)
    ThemeColor.ORANGE -> getColor(R.color.palette_dark_orange, null)
    ThemeColor.RED -> getColor(R.color.palette_dark_red, null)
    ThemeColor.PINK -> getColor(R.color.palette_dark_pink, null)
    ThemeColor.PURPLE -> getColor(R.color.palette_dark_purple, null)
    ThemeColor.BLUE -> getColor(R.color.palette_dark_blue, null)
    ThemeColor.ICE -> getColor(R.color.palette_dark_ice, null)
    ThemeColor.TEAL -> getColor(R.color.palette_dark_teal, null)
    ThemeColor.GREEN -> getColor(R.color.palette_dark_green, null)
}

fun Resources.light(color: ThemeColor, default: Int): Int = when (color) {
        ThemeColor.DEFAULT -> default
        ThemeColor.GREY -> getColor(R.color.palette_light_grey, null)
        ThemeColor.YELLOW -> getColor(R.color.palette_light_yellow, null)
        ThemeColor.ORANGE -> getColor(R.color.palette_light_orange, null)
        ThemeColor.RED -> getColor(R.color.palette_light_red, null)
        ThemeColor.PINK -> getColor(R.color.palette_light_pink, null)
        ThemeColor.PURPLE -> getColor(R.color.palette_light_purple, null)
        ThemeColor.BLUE -> getColor(R.color.palette_light_blue, null)
        ThemeColor.ICE -> getColor(R.color.palette_light_ice, null)
        ThemeColor.TEAL -> getColor(R.color.palette_light_teal, null)
        ThemeColor.GREEN -> getColor(R.color.palette_light_green, null)
}

fun Resources.lighter(color: ThemeColor, default: Int): Int = when (color) {
        ThemeColor.DEFAULT -> default
        ThemeColor.GREY -> getColor(R.color.palette_very_light_grey, null)
        ThemeColor.YELLOW -> getColor(R.color.palette_very_light_yellow, null)
        ThemeColor.ORANGE -> getColor(R.color.palette_very_light_orange, null)
        ThemeColor.RED -> getColor(R.color.palette_very_light_red, null)
        ThemeColor.PINK -> getColor(R.color.palette_very_light_pink, null)
        ThemeColor.PURPLE -> getColor(R.color.palette_very_light_purple, null)
        ThemeColor.BLUE -> getColor(R.color.palette_very_light_blue, null)
        ThemeColor.ICE -> getColor(R.color.palette_very_light_ice, null)
        ThemeColor.TEAL -> getColor(R.color.palette_very_light_teal, null)
        ThemeColor.GREEN -> getColor(R.color.palette_very_light_green, null)
}