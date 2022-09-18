package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.values
import com.anytypeio.anytype.presentation.sets.model.Viewer
import timber.log.Timber


suspend fun DVViewer.buildGalleryViews(
    objects: List<Id>,
    relations: List<Relation>,
    details: Map<Id, Block.Fields>,
    urlBuilder: UrlBuilder,
    store: ObjectStore
) : List<Viewer.GalleryView.Item> {
    val filteredRelations = viewerRelations.mapNotNull { setting ->
        if (setting.isVisible && setting.key != Relations.NAME) {
            relations.find { it.key == setting.key }
        } else {
            null
        }
    }
    return objects.mapNotNull { id -> store.get(id) }.map { obj ->
        if (coverRelationKey == null) {
            Viewer.GalleryView.Item.Default(
                objectId = obj.id,
                relations = obj.values(
                    relations = filteredRelations,
                    urlBuilder = urlBuilder,
                    details = details,
                    settings = viewerRelations,
                ),
                bigIcon = cardSize != DVViewerCardSize.SMALL,
                hideIcon = hideIcon,
                name = obj.getProperName(),
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.layout,
                    builder = urlBuilder
                )
            )
        } else {

            var cover : CoverView? = null

            var coverColor: String? = null
            var coverImage: Url? = null
            var coverGradient: String? = null

            if (coverRelationKey == Relations.PAGE_COVER) {
                when (obj.coverType?.code) {
                    CoverType.UPLOADED_IMAGE.code -> {
                        coverImage = obj.coverId?.let { id ->
                            urlBuilder.image(id)
                        }
                    }
                    CoverType.COLOR.code -> {
                        coverColor = obj.coverId
                    }
                    CoverType.GRADIENT.code -> {
                        coverGradient = obj.coverId
                    }
                    else -> Timber.d("Missing cover type: $type")
                }
            } else {
                val previewRelation = relations.find { it.key == coverRelationKey }
                if (previewRelation != null && previewRelation.format == Relation.Format.FILE) {
                    val ids : List<Id> = when(val value = obj.map[previewRelation.key]) {
                        is Id -> listOf(value)
                        is List<*> -> value.typeOf()
                        else -> emptyList()
                    }
                    val previewId = ids.find { id ->
                        val preview = details[id]
                        preview != null && preview.type.contains(ObjectType.IMAGE_URL)
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

            if (cover != null) {
                Viewer.GalleryView.Item.Cover(
                    objectId = obj.id,
                    relations = obj.values(
                        relations = filteredRelations,
                        urlBuilder = urlBuilder,
                        details = details,
                        settings = viewerRelations,
                    ),
                    hideIcon = hideIcon,
                    name = obj.getProperName(),
                    icon = ObjectIcon.from(
                        obj = obj,
                        layout = obj.layout,
                        builder = urlBuilder
                    ),
                    cover = cover,
                    fitImage = coverFit,
                    isCoverLarge = cardSize == DVViewerCardSize.LARGE
                )
            } else {
                Viewer.GalleryView.Item.Default(
                    objectId = obj.id,
                    relations = obj.values(
                        relations = filteredRelations,
                        urlBuilder = urlBuilder,
                        details = details,
                        settings = viewerRelations,
                    ),
                    bigIcon = cardSize != DVViewerCardSize.SMALL,
                    hideIcon = hideIcon,
                    name = obj.getProperName(),
                    icon = ObjectIcon.from(
                        obj = obj,
                        layout = obj.layout,
                        builder = urlBuilder
                    )
                )
            }
        }
    }
}