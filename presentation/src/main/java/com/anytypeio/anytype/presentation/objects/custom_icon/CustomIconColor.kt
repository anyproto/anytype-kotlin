package com.anytypeio.anytype.presentation.objects.custom_icon

enum class CustomIconColor(val rawValue: Int) {
    Gray(0),
    Yellow(1),
    Amber(2),
    Red(3),
    Pink(4),
    Purple(5),
    Blue(6),
    Sky(7),
    Teal(8),
    Green(9);

    val iconOption: Int
        get() = rawValue + 1

    val id: Int
        get() = rawValue

    companion object {
        val DEFAULT: CustomIconColor = Gray

        fun fromIconOption(iconOption: Int?): CustomIconColor? {
            return iconOption?.let { option ->
                CustomIconColor.entries.find { it.rawValue == option - 1 }
            }
        }
    }
}