package com.anytypeio.anytype.feature_os_widgets.mapper

import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceIcon
import com.anytypeio.anytype.feature_os_widgets.model.OsWidgetSpaceItem
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceEntity

/**
 * Maps persistence entity to domain/UI model.
 */
fun OsWidgetSpaceEntity.toDomain(): OsWidgetSpaceItem {
    val color = SystemColor.color(iconColorIndex)
    val imageUrl = iconImageUrl
    val icon = if (imageUrl != null) {
        OsWidgetSpaceIcon.Image(url = imageUrl, color = color)
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
    val (imageUrl, colorIndex) = when (icon) {
        is OsWidgetSpaceIcon.Image -> icon.url to icon.color.index
        is OsWidgetSpaceIcon.Placeholder -> null to icon.color.index
    }

    return OsWidgetSpaceEntity(
        spaceId = spaceId,
        name = name,
        iconImageUrl = imageUrl,
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
