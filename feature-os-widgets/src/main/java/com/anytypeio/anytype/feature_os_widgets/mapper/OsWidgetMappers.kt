package com.anytypeio.anytype.feature_os_widgets.mapper

import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceIcon
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceItem
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetSpaceEntity

/**
 * Maps persistence entity to domain/UI model.
 */
fun OsWidgetSpaceEntity.toDomain(): OsWidgetSpaceItem {
    val color = SystemColor.color(iconColorIndex)
    val icon = if (iconImageHash != null) {
        OsWidgetSpaceIcon.Image(hash = iconImageHash, color = color)
    } else {
        OsWidgetSpaceIcon.Placeholder(color = color, name = name)
    }

    return OsWidgetSpaceItem(
        spaceId = spaceId,
        name = name,
        icon = icon,
        spaceUxType = SpaceUxType.entries.getOrElse(spaceUxType) { SpaceUxType.DATA }
    )
}

/**
 * Maps domain/UI model to persistence entity.
 */
fun OsWidgetSpaceItem.toEntity(): OsWidgetSpaceEntity {
    val (imageHash, colorIndex) = when (icon) {
        is OsWidgetSpaceIcon.Image -> icon.hash to icon.color.index
        is OsWidgetSpaceIcon.Placeholder -> null to icon.color.index
    }

    return OsWidgetSpaceEntity(
        spaceId = spaceId,
        name = name,
        iconImageHash = imageHash,
        iconColorIndex = colorIndex,
        spaceUxType = spaceUxType.ordinal
    )
}

/**
 * Maps a list of entities to domain models.
 */
fun List<OsWidgetSpaceEntity>.toDomain(): List<OsWidgetSpaceItem> = map { it.toDomain() }

/**
 * Maps a list of domain models to entities.
 */
fun List<OsWidgetSpaceItem>.toEntities(): List<OsWidgetSpaceEntity> = map { it.toEntity() }
