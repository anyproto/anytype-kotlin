package com.anytypeio.anytype.feature_object_type.models

import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.templates.TemplateView

//region Mapping
fun ObjectWrapper.Basic.toTemplateView(
    objectId: Id,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
): TemplateView.Template {
    val coverContainer = if (coverType != CoverType.NONE) {
        BasicObjectCoverWrapper(this)
            .getCover(urlBuilder, coverImageHashProvider)
    } else {
        null
    }
    return TemplateView.Template(
        id = id,
        name = name.orEmpty(),
        targetTypeId = TypeId(targetObjectType.orEmpty()),
        emoji = if (!iconEmoji.isNullOrBlank()) iconEmoji else null,
        image = iconImage?.takeIf { it.isNotBlank() }?.let { urlBuilder.thumbnail(it) },
        layout = layout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = false,
        targetTypeKey = TypeKey(objectId)
    )
}
//endregion

