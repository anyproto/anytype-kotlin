package com.anytypeio.anytype.feature_date.mapping

import com.anytypeio.anytype.core_models.RelationListWithValueItem
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsItem
import timber.log.Timber

suspend fun List<RelationListWithValueItem>.toUiFieldsItem(
    storeOfRelations: StoreOfRelations
): List<UiFieldsItem.Item> {
    return this
        .sortedByDescending { it.key.key == Relations.MENTIONS }
        .mapNotNull { item ->
            val relation = storeOfRelations.getByKey(item.key.key)
            if (relation == null) {
                Timber.w("Relation ${item.key.key} not found in the relation store")
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