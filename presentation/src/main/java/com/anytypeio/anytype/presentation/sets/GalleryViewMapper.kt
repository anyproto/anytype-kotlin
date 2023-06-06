package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds.IMAGE
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.values
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.sets.model.Viewer


suspend fun DVViewer.buildGalleryViews(
    objectIds: List<Id>,
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    objectStore: ObjectStore,
    objectOrderIds: List<Id>
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
                    details = details,
                    store = objectStore,
                    relations = relations,
                    filteredRelations = filteredRelations,
                    isLargeSize = cardSize == DVViewerCardSize.LARGE
                )
            } else {
                obj.mapToDefaultItem(
                    hideIcon = hideIcon,
                    urlBuilder = urlBuilder,
                    details = details,
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
    details: Map<Id, Block.Fields>,
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
            details = details,
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
    relations: List<ObjectWrapper.Relation>,
    details: Map<Id, Block.Fields>,
    coverImageHashProvider: CoverImageHashProvider,
    urlBuilder: UrlBuilder,
    store: ObjectStore,
    filteredRelations: List<ObjectWrapper.Relation>,
    isLargeSize: Boolean
): Viewer.GalleryView.Item {
    val obj = this
    var cover: CoverView? = null

    var coverColor: CoverColor? = null
    var coverImage: Url? = null
    var coverGradient: String? = null

    if (obj.coverType != CoverType.NONE) {
        val coverContainer = BasicObjectCoverWrapper(obj)
            .getCover(urlBuilder, coverImageHashProvider)

        coverColor = coverContainer.coverColor
        coverImage = coverContainer.coverImage
        coverGradient = coverContainer.coverGradient
    } else {
        val previewRelation = relations.find { it.key == dvViewer.coverRelationKey }
        if (previewRelation != null && previewRelation.format == Relation.Format.FILE) {
            val ids: List<Id> = when (val value = obj.map[previewRelation.key]) {
                is Id -> listOf(value)
                is List<*> -> value.typeOf()
                else -> emptyList()
            }
            val previewId = ids.find { id ->
                val preview = details[id]
                preview != null && preview.type.contains(IMAGE)
            }
            if (previewId != null) {
                coverImage = urlBuilder.image(previewId)
            }
        }
    }

    when {
        coverImage != null -> {
            cover = CoverView.Image(coverImage)
        }
        coverColor != null -> {
            cover = CoverView.Color(coverColor)
        }
        coverGradient != null -> {
            cover = CoverView.Gradient(coverGradient)
        }
    }

    return Viewer.GalleryView.Item.Cover(
        objectId = obj.id,
        relations = obj.values(
            relations = filteredRelations,
            urlBuilder = urlBuilder,
            details = details,
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