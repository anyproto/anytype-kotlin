package com.anytypeio.anytype.feature_date.models

import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.getProperType
import timber.log.Timber

suspend fun List<RelationListWithValueItem>.toUiHorizontalListItems(
    storeOfRelations: StoreOfRelations
): List<UiHorizontalListItem.Item> {
    return this
        .sortedByDescending { it.key.key == Relations.MENTIONS }
        .mapNotNull { item ->
            val relation = storeOfRelations.getByKey(item.key.key)
            if (relation != null) {
                UiHorizontalListItem.Item(
                    id = item.key.key,
                    key = item.key,
                    title = relation.name.orEmpty(),
                    relationFormat = relation.format
                )
            } else {
                Timber.e("Relation ${item.key.key} not found in the relation store")
                null
            }
        }
}

fun ObjectWrapper.Basic.toUiVerticalListItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): UiVerticalListItem {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.layout ?: ObjectType.Layout.BASIC
    return UiVerticalListItem.Item(
        id = obj.id,
        space = space,
        name = obj.getProperName(),
        type = typeUrl,
        typeName = objectTypes.firstOrNull { type ->
            if (isProfile) {
                type.uniqueKey == ObjectTypeUniqueKeys.PROFILE
            } else {
                type.id == typeUrl
            }
        }?.name,
        layout = layout,
        icon = obj.objectIcon(builder = urlBuilder)
    )
}