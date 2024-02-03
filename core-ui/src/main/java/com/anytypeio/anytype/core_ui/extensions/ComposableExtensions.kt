package com.anytypeio.anytype.core_ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
