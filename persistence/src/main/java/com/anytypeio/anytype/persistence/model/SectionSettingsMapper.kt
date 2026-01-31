package com.anytypeio.anytype.persistence.model

import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_models.WidgetSectionConfig
import com.anytypeio.anytype.core_models.WidgetSections
import com.anytypeio.anytype.persistence.SectionConfigProto
import com.anytypeio.anytype.persistence.SectionSettings as ProtoSectionSettings

/**
 * Convert proto SectionSettings to domain WidgetSections
 */
fun ProtoSectionSettings.toDomain(): WidgetSections {
    return WidgetSections(
        sections = sections.map { it.toDomain() }
    )
}

/**
 * Convert domain WidgetSections to proto SectionSettings
 */
fun WidgetSections.toProto(): ProtoSectionSettings {
    return ProtoSectionSettings(
        sections = sections.map { it.toProto() }
    )
}

/**
 * Convert proto SectionConfigProto to domain WidgetSectionConfig
 */
fun SectionConfigProto.toDomain(): WidgetSectionConfig {
    val sectionType = try {
        WidgetSectionType.valueOf(sectionType)
    } catch (e: IllegalArgumentException) {
        WidgetSectionType.PINNED // fallback to default
    }
    
    return WidgetSectionConfig(
        id = sectionType,
        isVisible = isVisible,
        order = order,
        isUserConfigurable = sectionType.isUserConfigurable()
    )
}

/**
 * Convert domain WidgetSectionConfig to proto SectionConfigProto
 */
fun WidgetSectionConfig.toProto(): SectionConfigProto {
    return SectionConfigProto(
        sectionType = id.name,
        isVisible = isVisible,
        order = order
    )
}
