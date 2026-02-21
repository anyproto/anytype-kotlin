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

    /**
     * Determines if this section type can be configured by the user
     * UNREAD: Always visible, can't be toggled
     */
    fun isUserConfigurable(): Boolean {
        return this != UNREAD
    }
}

/**
 * Configuration for individual widget section
 * @property id Widget section type identifier
 * @property isVisible Whether the widget section is visible on the home screen
 * @property order Display order of the section (lower number = higher priority)
 * @property isUserConfigurable Whether this section can be configured by the user
 */
data class WidgetSectionConfig(
    val id: WidgetSectionType,
    val isVisible: Boolean,
    val order: Int,
    val isUserConfigurable: Boolean = id.isUserConfigurable()
) {
    companion object {
        fun default(type: WidgetSectionType, order: Int): WidgetSectionConfig {
            return WidgetSectionConfig(
                id = type,
                isVisible = true,
                order = order,
                isUserConfigurable = type.isUserConfigurable()
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
     * Ensures all known [WidgetSectionType] entries are present.
     * Missing types are inserted at their default position from [WidgetSectionType.DEFAULT_ORDER].
     */
    fun withDefaults(): WidgetSections {
        val existingTypes = sections.map { it.id }.toSet()
        val missing = WidgetSectionType.DEFAULT_ORDER
            .filter { it !in existingTypes }
        if (missing.isEmpty()) return this
        val defaultIndex = WidgetSectionType.DEFAULT_ORDER
            .withIndex()
            .associate { (index, type) -> type to index }
        val merged = (sections + missing.map { type ->
            WidgetSectionConfig.default(type, defaultIndex.getValue(type))
        }).sortedBy { defaultIndex[it.id] ?: it.order }
            .mapIndexed { index, config -> config.copy(order = index) }
        return copy(sections = merged)
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
