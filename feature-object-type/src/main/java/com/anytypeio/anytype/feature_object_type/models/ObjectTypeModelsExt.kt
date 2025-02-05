package com.anytypeio.anytype.feature_object_type.models

import android.R
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_object_type.models.UiFieldsListItem.FieldItem
import com.anytypeio.anytype.feature_object_type.models.UiFieldsListItem.Section
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


/**
 * Maps a [Relation] to its corresponding [UiFieldsListItem] based on the relation format.
 */
private fun mapToUiFieldsListItem(
    relation: ObjectWrapper.Relation,
    canDrag: Boolean,
    stringResourceProvider: StringResourceProvider
): UiFieldsListItem? {
    return FieldItem(
        id = relation.id,
        fieldKey = relation.key,
        fieldTitle = relation.getName(stringResourceProvider),
        format = relation.format,
        canDrag = canDrag
    )
}

/**
 * Extension function to safely get a name for the relation.
 * If the name is blank, returns a default untitled title.
 */
private fun ObjectWrapper.Relation.getName(stringResourceProvider: StringResourceProvider): String =
    if (name.isNullOrBlank()) {
        stringResourceProvider.getUntitledObjectTitle()
    } else {
        name!!
    }

/**
 * Builds the list of UI fields to display.
 *
 * This helper maps recommended relation keys (for featured, sidebar, and hidden sections, etc.)
 * into their corresponding [UiFieldsListItem]s using the provided relations map.
 * For each section, if there are any items, a section header is inserted before the items.
 * Note that any relation with the key [Relations.DESCRIPTION] is explicitly filtered out.
 *
 * @param objType the type object that contains lists of recommended relation keys for each section.
 * @param relations a map of relation identifiers to their corresponding [ObjectWrapper.Relation]s.
 *                  Relations with the key [Relations.DESCRIPTION] are excluded from the UI.
 * @return a list of [UiFieldsListItem] organized into sections (featured, sidebar, hidden) for display.
 */
fun buildUiFieldsList(
    objType: ObjectWrapper.Type,
    relations: Map<Id, ObjectWrapper.Relation>,
    stringResourceProvider: StringResourceProvider
): List<UiFieldsListItem> {

    fun mapRelations(
        keys: List<String>,
        canDrag: Boolean,
        stringResourceProvider: StringResourceProvider
    ) = keys.mapNotNull { key ->
        if (key != Relations.DESCRIPTION) {
            relations[key]?.let { mapToUiFieldsListItem(it, canDrag, stringResourceProvider) }
        } else {
            null
        }
    }

    val featuredItems = mapRelations(objType.recommendedFeaturedRelations, true, stringResourceProvider)
    val sidebarItems = mapRelations(objType.recommendedRelations, true, stringResourceProvider)
    val hiddenItems = mapRelations(objType.recommendedHiddenRelations, false, stringResourceProvider)

    return buildList {
        add(Section.Header())
        addAll(featuredItems)

        add(Section.FieldsMenu())
        addAll(sidebarItems)

//            add(Section.Hidden)
        addAll(hiddenItems)
    }
}

