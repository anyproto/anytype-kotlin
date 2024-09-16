package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.values
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.CoverContainer
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.sets.model.Viewer


suspend fun DVViewer.buildGalleryViews(
    objectIds: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    objectStore: ObjectStore,
    objectOrderIds: List<Id>,
    storeOfRelations: StoreOfRelations
): List<Viewer.GalleryView.Item> {

    val filteredRelations = viewerRelations.mapNotNull { setting ->
        if (setting.isVisible && setting.key != Relations.NAME) {
            relations.find { it.key == setting.key }
        } else {
            null
        }
    }

    val hasCover = !coverRelationKey.isNullOrEmpty()

    val orderMap = objectOrderIds.mapIndexed { index, id -> id to index }.toMap()

    return objectIds
        .mapNotNull { objectStore.get(it) }
        .map { obj ->
            if (hasCover) {
                obj.mapToCoverItem(
                    dvViewer = this,
                    coverImageHashProvider = coverImageHashProvider,
                    urlBuilder = urlBuilder,
                    store = objectStore,
                    filteredRelations = filteredRelations,
                    isLargeSize = true,
                    storeOfRelations = storeOfRelations
                )
            } else {
                obj.mapToDefaultItem(
                    hideIcon = hideIcon,
                    urlBuilder = urlBuilder,
                    viewerRelations = viewerRelations,
                    store = objectStore,
                    filteredRelations = filteredRelations
                )
            }
        }
        .sortedBy { item -> orderMap[item.objectId] }
}

private suspend fun ObjectWrapper.Basic.mapToDefaultItem(
    hideIcon: Boolean,
    urlBuilder: UrlBuilder,
    viewerRelations: List<DVViewerRelation>,
    store: ObjectStore,
    filteredRelations: List<ObjectWrapper.Relation>
): Viewer.GalleryView.Item {
    val obj = this
    return Viewer.GalleryView.Item.Default(
        objectId = obj.id,
        relations = obj.values(
            relations = filteredRelations,
            urlBuilder = urlBuilder,
            settings = viewerRelations,
            storeOfObjects = store
        ),
        hideIcon = hideIcon,
        name = obj.getProperName(),
        icon = ObjectIcon.getEditorLinkToObjectIcon(
            obj = obj,
            layout = obj.layout,
            builder = urlBuilder
        )
    )
}

private suspend fun ObjectWrapper.Basic.mapToCoverItem(
    dvViewer: DVViewer,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    filteredRelations: List<ObjectWrapper.Relation>,
    isLargeSize: Boolean,
    storeOfRelations: StoreOfRelations
): Viewer.GalleryView.Item {
    val obj = this

    val coverContainer = getCoverContainer(
        obj = obj,
        dvViewer = dvViewer,
        coverImageHashProvider = coverImageHashProvider,
        urlBuilder = urlBuilder,
        store = store,
        storeOfRelations = storeOfRelations,
        isLargeSize = isLargeSize
    )
    val cover = createCoverView(coverContainer = coverContainer)

    return Viewer.GalleryView.Item.Cover(
        objectId = obj.id,
        relations = obj.values(
            relations = filteredRelations,
            urlBuilder = urlBuilder,
            settings = dvViewer.viewerRelations,
            storeOfObjects = store
        ),
        hideIcon = dvViewer.hideIcon,
        name = obj.getProperName(),
        icon = ObjectIcon.getEditorLinkToObjectIcon(
            obj = obj,
            layout = obj.layout,
            builder = urlBuilder
        ),
        cover = cover,
        fitImage = dvViewer.coverFit,
        isLargeSize = isLargeSize
    )
}

private suspend fun getCoverContainer(
    obj: ObjectWrapper.Basic,
    dvViewer: DVViewer,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    storeOfRelations: StoreOfRelations,
    isLargeSize: Boolean
): CoverContainer {
    val coverRelationKey = dvViewer.coverRelationKey
    return when (coverRelationKey) {
        Relations.PAGE_COVER -> {
            BasicObjectCoverWrapper(obj).getCover(
                urlBuilder = urlBuilder,
                coverImageHashProvider = coverImageHashProvider)
        }
        else -> {
            getCoverFromRelationOrLayout(
                obj = obj,
                dvViewer = dvViewer,
                urlBuilder = urlBuilder,
                dependedObjects = store.getAll(),
                storeOfRelations = storeOfRelations,
                isLargeSize = isLargeSize
            )
        }
    }
}

suspend fun getCoverFromRelationOrLayout(
    obj: ObjectWrapper.Basic,
    dvViewer: DVViewer,
    urlBuilder: UrlBuilder,
    dependedObjects: List<ObjectWrapper.Basic>,
    storeOfRelations: StoreOfRelations,
    isLargeSize: Boolean
): CoverContainer {
    val coverRelationKey = dvViewer.coverRelationKey
    var coverImage: Url? = null
    if (coverRelationKey != null) {
        val relation = storeOfRelations.getByKey(coverRelationKey)
        coverImage = relation?.let {
            getCoverImageFromRelation(
                relation = it,
                obj = obj,
                coverRelationKey = coverRelationKey,
                dependedObjects = dependedObjects,
                urlBuilder = urlBuilder,
                isLargeSize = isLargeSize
            )
        }
    }
    if (coverImage == null && obj.layout == ObjectType.Layout.IMAGE) {
        coverImage = if (isLargeSize) {
            urlBuilder.large(obj.id)
        } else {
            urlBuilder.medium(obj.id)
        }
    }
    return CoverContainer(coverImage = coverImage)
}

private fun getCoverImageFromRelation(
    relation: ObjectWrapper.Relation,
    obj: ObjectWrapper.Basic,
    coverRelationKey: String,
    dependedObjects: List<ObjectWrapper.Basic>,
    urlBuilder: UrlBuilder,
    isLargeSize: Boolean
): Url? {
    if (relation.format == Relation.Format.FILE) {
        val ids: List<Id> = when (val value = obj.map[coverRelationKey]) {
            is Id -> listOf(value)
            is List<*> -> value.typeOf()
            else -> emptyList()
        }
        val previewId = ids.find { id ->
            val preview = dependedObjects.firstOrNull { it.id == id }
            preview != null && preview.layout == ObjectType.Layout.IMAGE
        }
        if (!previewId.isNullOrBlank()) {
            return if (isLargeSize) {
                urlBuilder.large(previewId)
            } else {
                urlBuilder.large(previewId)
            }
        }
    }
    return null
}

private fun createCoverView(coverContainer: CoverContainer): CoverView? = when {
    coverContainer.coverImage != null -> CoverView.Image(coverContainer.coverImage)
    coverContainer.coverColor != null -> CoverView.Color(coverContainer.coverColor)
    coverContainer.coverGradient != null -> CoverView.Gradient(coverContainer.coverGradient)
    else -> null
}
