package com.anytypeio.anytype.core_models

/**
 * Widget section type identifier for home screen widget sections
 */
enum class WidgetSectionType {
    UNREAD,
    PINNED,
    OBJECTS,
    RECENTLY_EDITED,
    BIN;

    companion object {
        val DEFAULT_ORDER = listOf(UNREAD, PINNED, OBJECTS, RECENTLY_EDITED, BIN)
    }
}

/**
 * Configuration for individual widget section
 * @property id Widget section type identifier
 * @property isVisible Whether the widget section is visible on the home screen
 * @property order Display order of the section (lower number = higher priority)
 */
data class WidgetSectionConfig(
    val id: WidgetSectionType,
    val isVisible: Boolean,
    val order: Int
) {
    companion object {
        fun default(type: WidgetSectionType, order: Int): WidgetSectionConfig {
            return WidgetSectionConfig(
                id = type,
                isVisible = true,
                order = order
            )
        }
    }
}

/**
 * Widget sections settings for a space
 * @property sections List of widget section configurations
 */
data class WidgetSections(
    val sections: List<WidgetSectionConfig>
) {
    companion object {
        /**
         * Default widget sections with all sections visible in default order
         */
        fun default(): WidgetSections {
            return WidgetSections(
                sections = WidgetSectionType.DEFAULT_ORDER.mapIndexed { index, type ->
                    WidgetSectionConfig.default(type, index)
                }
            )
        }
    }

    /**
     * Get ordered list of visible sections
     */
    fun getVisibleSections(): List<WidgetSectionType> {
        return sections
            .filter { it.isVisible }
            .sortedBy { it.order }
            .map { it.id }
    }

    /**
     * Check if a specific section is visible
     */
    fun isSectionVisible(type: WidgetSectionType): Boolean {
        return sections.find { it.id == type }?.isVisible ?: true
    }
}
