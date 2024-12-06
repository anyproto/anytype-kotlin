package com.anytypeio.anytype.core_models

sealed class RelativeDate {
    abstract val initialTimeInMillis: TimeInMillis
    abstract val dayOfWeek: DayOfWeekCustom

    /** Represents an unset date (timestamp = 0.0) */
    data object Empty : RelativeDate() {
        override val initialTimeInMillis: TimeInMillis = 0
        override val dayOfWeek: DayOfWeekCustom = DayOfWeekCustom.MONDAY // Default value
    }

    data class Today(
        override val initialTimeInMillis: TimeInMillis,
        override val dayOfWeek: DayOfWeekCustom
    ) : RelativeDate()

    data class Tomorrow(
        override val initialTimeInMillis: TimeInMillis,
        override val dayOfWeek: DayOfWeekCustom
    ) : RelativeDate()

    data class Yesterday(
        override val initialTimeInMillis: TimeInMillis,
        override val dayOfWeek: DayOfWeekCustom
    ) : RelativeDate()

    data class Other(
        override val initialTimeInMillis: TimeInMillis,
        override val dayOfWeek: DayOfWeekCustom,
        val formattedDate: String,
        val formattedTime: String
    ) : RelativeDate()
}

enum class DayOfWeekCustom {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}