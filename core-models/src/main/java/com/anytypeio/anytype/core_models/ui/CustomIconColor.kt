package com.anytypeio.anytype.core_models.ui

/**
 * Enum representing custom icon colors with associated raw integer values.
 *
 * Each color has a corresponding [iconOption] and [id]. The [iconOption] is
 * computed as the raw value plus one, while the [id] is equal to the raw value.
 * The default color is specified by [DEFAULT].
 */
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
    Green(9),
    Transparent(10);

    /**
     * Returns the icon option corresponding to this color.
     *
     * This is calculated as the [rawValue] plus one.
     */
    val iconOption: Int
        get() = rawValue + 1

    /**
     * Returns the id corresponding to this color.
     *
     * The id is equal to the [rawValue].
     */
    val id: Int
        get() = rawValue

    companion object {
        /**
         * Default color used when a specific color is not provided.
         */
        val DEFAULT: CustomIconColor = Gray

        /**
         * Returns the [CustomIconColor] associated with the provided [iconOption].
         *
         * If [iconOption] is null or does not match any [CustomIconColor],
         * the [DEFAULT] color is returned.
         *
         * @param iconOption The icon option integer, which may be null.
         * @return The matching [CustomIconColor] or [DEFAULT] if no match is found.
         */
        fun fromIconOption(iconOption: Int?): CustomIconColor {
            return if (iconOption == null) {
                DEFAULT
            } else {
                CustomIconColor.entries.find { it.iconOption == iconOption } ?: DEFAULT
            }
        }
    }
}
