package com.anytypeio.anytype.presentation.objects.custom_icon

/**
 * Enum representing custom icon colors with associated raw integer values.
 *
 * Each color has a corresponding [iconOption] and [id]. The [iconOption] is
 * computed as the raw value plus one, while the [id] is equal to the raw value.
 * The default color is specified by [DEFAULT].
 */
enum class CustomIconColor(val rawValue: Int) {
    /** Gray color with a raw value of 0. */
    Gray(0),

    /** Yellow color with a raw value of 1. */
    Yellow(1),

    /** Amber color with a raw value of 2. */
    Amber(2),

    /** Red color with a raw value of 3. */
    Red(3),

    /** Pink color with a raw value of 4. */
    Pink(4),

    /** Purple color with a raw value of 5. */
    Purple(5),

    /** Blue color with a raw value of 6. */
    Blue(6),

    /** Sky color with a raw value of 7. */
    Sky(7),

    /** Teal color with a raw value of 8. */
    Teal(8),

    /** Green color with a raw value of 9. */
    Green(9);

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