package com.anytypeio.anytype.core_models


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


enum class SystemColor(
    val index: Int
) {
    GRAY(index = 0),
    YELLOW(index = 1),
    AMBER(index = 2),
    RED(index = 3),
    PINK(index = 4),
    PURPLE(index = 5),
    BLUE(index = 6),
    SKY(index = 7),
    TEAL(index = 8),
    GREEN(index = 9);

    companion object {
        fun color(idx: Int): SystemColor {
            if (idx < 0) {
                return YELLOW
            } else {
                val values = SystemColor.entries
                val size = values.size
                val normalizedIndex = idx % size
                return values[normalizedIndex]
            }
        }
    }
}