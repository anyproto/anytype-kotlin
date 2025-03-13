package com.anytypeio.anytype.presentation.objects.custom_icon

data class CustomIcon(val rawValue: String) {

    val stringRepresentation: String
        get() = buildString {
            rawValue.forEachIndexed { index, c ->
                if (c.isUpperCase() && index > 0) append("_")
                append(c.lowercaseChar())
            }
        }

    // 'id' property, simply the rawValue
    val id: String get() = rawValue

    companion object {
        // Predefined instances for each icon.
        // For brevity, only a few are shown. Add the rest as needed.
        val accessibility = CustomIcon("accessibility")
        val addCircle = CustomIcon("addCircle")
        val airplane = CustomIcon("airplane")
        val alarm = CustomIcon("alarm")
        val albums = CustomIcon("albums")
        val alertCircle = CustomIcon("alertCircle")
        // ... include all other icons here ...
        val woman = CustomIcon("woman")

        // List of all available icons
        val allIcons = listOf(
            accessibility, addCircle, airplane, alarm, albums, alertCircle,
            // ... add the remaining icons in the same order ...
            woman
        )

        fun fromString(string: String): CustomIcon? {
            val processed = string.split("_").mapIndexed { index, part ->
                if (index == 0) part.lowercase() else part.replaceFirstChar { it.uppercase() }
            }.joinToString(separator = "")
            return allIcons.find { it.rawValue == processed }
        }
    }
}