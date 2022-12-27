package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName


const val MAX_SNIPPET_SIZE = 30

fun Map<Id, Block.Fields>.updateFields(update: Map<Id, Block.Fields>): Map<Id, Block.Fields> {
    val result = this.toMutableMap()
    for ((key, value) in update) {
        result[key] = value
    }
    return result
}

fun Map<Id, Block.Fields>.getProperObjectName(id: Id?): String? {
    if (id == null) return null
    val layoutCode = this[id]?.layout?.toInt()
    return if (layoutCode == ObjectType.Layout.NOTE.code) {
        this[id]?.snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        this[id]?.name
    }
}

fun ObjectWrapper.Basic.getProperObjectName(): String? {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.replace("\n", " ")?.take(MAX_SNIPPET_SIZE)
    } else {
        name
    }
}

suspend fun List<Id>.mapToFavorites(
    blocks: List<Block>,
    objectStore: ObjectStore,
    urlBuilder: UrlBuilder
): List<DashboardView> {
    return mapNotNull { s: Id ->
        val block = blocks.firstOrNull { it.id == s }
        val content = block?.content
        if (content != null && content is Block.Content.Link) {
            val obj = objectStore.get(content.target)
            if (obj != null) {
                val layout = obj.layout
                if (layout == ObjectType.Layout.SET) {
                    DashboardView.ObjectSet(
                        id = block.id,
                        target = obj.id,
                        title = obj.getProperName(),
                        isArchived = obj.isArchived ?: false,
                        isLoading = false,
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        )
                    )
                } else {
                    DashboardView.Document(
                        id = block.id,
                        target = obj.id,
                        title = obj.getProperName(),
                        isArchived = obj.isArchived ?: false,
                        isLoading = false,
                        emoji = obj.iconEmoji,
                        image = obj.iconImage?.let { urlBuilder.thumbnail(it) },
                        type = obj.type.firstOrNull(),
                        typeName = obj.getTypeName(objectStore),
                        layout = obj.layout,
                        done = obj.done,
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = urlBuilder
                        )
                    )
                }
            } else {
                null
            }
        } else {
            null
        }
    }
}

fun List<Block>.sortByIds(
    ids: List<String>
): List<Block> {
    val orderedByIds = ids.withIndex().associate { it.value to it.index }
    return this.sortedBy { orderedByIds[it.id] }.filter { ids.contains(it.id) }
}