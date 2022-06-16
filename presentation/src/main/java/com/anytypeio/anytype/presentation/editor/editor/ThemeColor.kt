package com.anytypeio.anytype.presentation.editor.editor

import android.content.res.Resources.Theme

/**
 * @property code color code name
 */
enum class ThemeColor(
    val code: String,
) {
    DEFAULT(
        code = "default",
    ),
    GREY(
        code = "grey",
    ),
    YELLOW(
        code = "yellow",
    ),
    ORANGE(
        code = "orange",
    ),
    RED(
        code = "red",
    ),
    PINK(
        code = "pink",
    ),
    PURPLE(
        code = "purple",
    ),
    BLUE(
        code = "blue",
    ),
    ICE(
        code = "ice",
    ),
    TEAL(
        code = "teal",
    ),
    LIME(
        code = "lime",
    );

    companion object {
        fun fromCode(string: String): ThemeColor {
            return values().singleOrNull { it.code == string } ?: DEFAULT
        }
    }
}