package com.anytypeio.anytype.presentation.editor.editor

import android.graphics.Color

/**
 * @property title color code name
 * @property text text color integer for text styling
 * @property background background color integer for background/highlight styling
 */
enum class ThemeColor(
    val title: String,
    val text: Int,
    val background: Int
) {
    DEFAULT(
        title = "default",
        text = Color.parseColor("#2C2B27"),
        background = Color.parseColor("#FFFFFF")
    ),
    GREY(
        title = "grey",
        text = Color.parseColor("#ACA996"),
        background = Color.parseColor("#F3F2EC")
    ),
    YELLOW(
        title = "yellow",
        text = Color.parseColor("#ECD91B"),
        background = Color.parseColor("#FEF9CC")
    ),
    ORANGE(
        title = "orange",
        text = Color.parseColor("#FFB522"),
        background = Color.parseColor("#FEF3C5")
    ),
    RED(
        title = "red",
        text = Color.parseColor("#F55522"),
        background = Color.parseColor("#FFEBE5")
    ),
    PINK(
        title = "pink",
        text = Color.parseColor("#E51CA0"),
        background = Color.parseColor("#FEE3F5")
    ),
    PURPLE(
        title = "purple",
        text = Color.parseColor("#AB50CC"),
        background = Color.parseColor("#F4E3FA")
    ),
    BLUE(
        title = "blue",
        text = Color.parseColor("#3E58EB"),
        background = Color.parseColor("#E4E7FC")
    ),
    ICE(
        title = "ice",
        text = Color.parseColor("#2AA7EE"),
        background = Color.parseColor("#D6EFFD")
    ),
    TEAL(
        title = "teal",
        text = Color.parseColor("#0FC8BA"),
        background = Color.parseColor("#D6F5F3")
    ),
    GREEN(
        title = "lime",
        text = Color.parseColor("#57C600"),
        background = Color.parseColor("#E3F7D0")
    );
}