package com.anytypeio.anytype.feature_date.mapping

import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsItem
import com.anytypeio.anytype.feature_date.viewmodel.UiObjectsListItem
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.getProperType
import timber.log.Timber

suspend fun List<RelationListWithValueItem>.toUiFieldsItem(
    storeOfRelations: StoreOfRelations
): List<UiFieldsItem.Item> {
    return this
        .sortedByDescending { it.key.key == Relations.MENTIONS }
        .mapNotNull { item ->
            val relation = storeOfRelations.getByKey(item.key.key)
            if (relation == null) {
                Timber.e("Relation ${item.key.key} not found in the relation store")
                return@mapNotNull null
            }
            if (relation.key == Relations.LINKS || relation.key == Relations.BACKLINKS) {
                Timber.w("Relation ${item.key.key} is LINKS or BACKLINKS")
                return@mapNotNull null
            }
            if (relation.key != Relations.MENTIONS && relation.isHidden == true) {
                Timber.w("Relation ${item.key.key} is hidden")
                return@mapNotNull null
            }
            if (relation.key == Relations.MENTIONS) {
                UiFieldsItem.Item.Mention(
                    id = item.key.key,
                    key = item.key,
                    title = relation.name.orEmpty(),
                    relationFormat = relation.format
                )
            } else {
                UiFieldsItem.Item.Default(
                    id = item.key.key,
                    key = item.key,
                    title = relation.name.orEmpty(),
                    relationFormat = relation.format
                )
            }
        }
}

fun ObjectWrapper.Basic.toUiObjectsListItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): UiObjectsListItem {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.layout ?: ObjectType.Layout.BASIC
    return UiObjectsListItem.Item(
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