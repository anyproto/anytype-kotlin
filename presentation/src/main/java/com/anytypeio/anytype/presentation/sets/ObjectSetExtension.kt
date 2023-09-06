package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVFilterUpdate
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVSortUpdate
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVViewerFields
import com.anytypeio.anytype.core_models.Event.Command.DataView.UpdateView.DVViewerRelationUpdate
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_utils.ext.addAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.mapInPlace
import com.anytypeio.anytype.core_utils.ext.moveAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.moveOnTop
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.ID_KEY
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.relations.isSystemKey
import com.anytypeio.anytype.presentation.relations.title
import com.anytypeio.anytype.presentation.relations.type
import com.anytypeio.anytype.presentation.relations.view
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_TYPE_UNSUPPORTED
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK

fun ObjectState.DataView.featuredRelations(
    ctx: Id,
    urlBuilder: UrlBuilder,
    relations: List<ObjectWrapper.Relation>
): BlockView.FeaturedRelation? {
    val block = blocks.find { it.content is Block.Content.FeaturedRelations }
    if (block != null) {
        val views = mutableListOf<ObjectRelationView>()
        val ids = details[ctx]?.featuredRelations ?: emptyList()
        views.addAll(
            mapFeaturedRelations(
                ctx = ctx,
                keys = ids,
                details = Block.Details(details),
                relations = relations,
                urlBuilder = urlBuilder
            )
        )
        return BlockView.FeaturedRelation(
            id = block.id,
            relations = views
        )
    } else {
        return null
    }
}

fun ObjectState.DataView.header(
    ctx: Id,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider
): SetOrCollectionHeaderState {
    val title = blocks.title()
    return if (title != null) {
        val wrapper = ObjectWrapper.Basic(
            map = details[ctx]?.map ?: emptyMap()
        )
        val featured = wrapper.featuredRelations
        SetOrCollectionHeaderState.Default(
            title = title(
                ctx = ctx,
                title = title,
                urlBuilder = urlBuilder,
                details = details,
                coverImageHashProvider = coverImageHashProvider
            ),
            description = if (featured.contains(Relations.DESCRIPTION)) {
                SetOrCollectionHeaderState.Description.Default(
                    description = wrapper.description.orEmpty()
                )
            } else {
                SetOrCollectionHeaderState.Description.None
            }
        )
    } else {
        return SetOrCollectionHeaderState.None
    }
}

private fun ObjectState.DataView.mapFeaturedRelations(
    ctx: Id,
    keys: List<String>,
    details: Block.Details,
    relations: List<ObjectWrapper.Relation>,
    urlBuilder: UrlBuilder
): List<ObjectRelationView> = keys.mapNotNull { key ->
    when (key) {
        Relations.DESCRIPTION -> null
        Relations.TYPE -> details.details[ctx]?.type?.firstOrNull()?.let { typeId ->
            val objectType = details.details[typeId]?.map?.let { ObjectWrapper.Type(it) }
            if (objectType?.isDeleted == true) {
                ObjectRelationView.ObjectType.Deleted(
                    id = typeId,
                    key = key,
                    featured = true,
                    readOnly = false,
                    system = key.isSystemKey()
                )
            } else {
                when (this) {
                    is ObjectState.DataView.Collection -> ObjectRelationView.ObjectType.Collection(
                        id = typeId,
                        key = key,
                        name = objectType?.name.orEmpty(),
                        featured = true,
                        readOnly = false,
                        type = typeId,
                        system = key.isSystemKey()
                    )
                    is ObjectState.DataView.Set -> ObjectRelationView.ObjectType.Set(
                        id = typeId,
                        key = key,
                        name = objectType?.name.orEmpty(),
                        featured = true,
                        readOnly = false,
                        type = typeId,
                        system = key.isSystemKey()
                    )
                    else -> null
                }
            }
        }
        Relations.SET_OF -> {
            val objectSet = ObjectWrapper.Basic(details.details[ctx]?.map.orEmpty())
            val sources = mutableListOf<ObjectView>()
            val source = objectSet.setOf.firstOrNull()
            if (source != null) {
                val wrapper = ObjectWrapper.Basic(details.details[source]?.map.orEmpty())
                if (!wrapper.isEmpty()) {
                    if (wrapper.isDeleted == true) {
                        ObjectRelationView.Source.Deleted(
                            id = details.details[ctx]?.id.orEmpty(),
                            key = key,
                            name = Relations.RELATION_NAME_EMPTY,
                            featured = true,
                            readOnly = false,
                            system = key.isSystemKey()
                        )
                    } else {
                        sources.add(wrapper.toObjectView(urlBuilder = urlBuilder))
                        ObjectRelationView.Source.Base(
                            id = details.details[ctx]?.id.orEmpty(),
                            key = key,
                            name = Relations.RELATION_NAME_EMPTY,
                            featured = true,
                            readOnly = wrapper.relationReadonlyValue ?: false,
                            sources = sources,
                            system = key.isSystemKey()
                        )
                    }
                } else {
                    ObjectRelationView.Source.Base(
                        id = details.details[ctx]?.id.orEmpty(),
                        key = key,
                        name = Relations.RELATION_NAME_EMPTY,
                        featured = true,
                        readOnly = wrapper.relationReadonlyValue ?: false,
                        sources = sources,
                        system = key.isSystemKey()
                    )
                }
            } else {
                ObjectRelationView.Source.Base(
                    id = details.details[ctx]?.id.orEmpty(),
                    key = key,
                    name = Relations.RELATION_NAME_EMPTY,
                    featured = true,
                    readOnly = false,
                    sources = sources,
                    system = key.isSystemKey()
                )
            }
        }
        else -> {
            val relation = relations.firstOrNull { it.key == key }
            relation?.view(
                details = details,
                values = details.details[ctx]?.map ?: emptyMap(),
                urlBuilder = urlBuilder,
                isFeatured = true
            )
        }
    }
}

fun List<DVRecord>.update(new: List<DVRecord>): List<DVRecord> {
    val update = new.associateBy { rec -> rec[ID_KEY] as String }
    val result = mutableListOf<DVRecord>()
    forEach { rec ->
        val id = rec[ID_KEY]
        if (update.containsKey(id)) {
            val updated = update[id]
            if (updated != null)
                result.add(updated)
            else
                result.add(rec)
        } else {
            result.add(rec)
        }
    }
    // TODO remove this part when middleware is fixed
    update.forEach { (id, record) ->
        val isAlreadyPresent = result.any { r -> r[ID_KEY] == id }
        if (!isAlreadyPresent) result.add(record)
    }
    return result
}

fun ObjectState.DataView.viewerById(currentViewerId: String?): DVViewer? {
    if (!isInitialized) return null
    return dataViewContent.viewers.find { it.id == currentViewerId }
        ?: dataViewContent.viewers.firstOrNull()
}

fun ObjectState.DataView.Collection.getObjectOrderIds(currentViewerId: String): List<Id> {
    return dataViewContent.objectOrders.find { it.view == currentViewerId }?.ids ?: emptyList()
}

suspend fun List<DVFilter>.updateFormatForSubscription(storeOfRelations: StoreOfRelations): List<DVFilter> {
    return map { f: DVFilter ->
        val r = storeOfRelations.getByKey(f.relation)
        if (r != null && r.relationFormat == RelationFormat.DATE) {
            f.copy(relationFormat = r.relationFormat)
        } else {
            f
        }
    }
}

fun List<SimpleRelationView>.filterHiddenRelations(): List<SimpleRelationView> =
    filter { !it.isHidden }

fun ObjectWrapper.Basic.toObjectView(urlBuilder: UrlBuilder): ObjectView = when (isDeleted) {
    true -> ObjectView.Deleted(id)
    else -> ObjectView.Default(
        id = id,
        name = getProperName(),
        icon = ObjectIcon.from(
            obj = this,
            layout = layout,
            builder = urlBuilder,
            objectTypeNoIcon = true
        ),
        types = type,
        isRelation = type.contains(ObjectTypeIds.RELATION)
    )
}

fun List<DVFilter>.updateFilters(updates: List<DVFilterUpdate>): List<DVFilter> {
    val filters = this.toMutableList()
    updates.forEach { update ->
        when (update) {
            is DVFilterUpdate.Add -> {
                filters.addAfterIndexInLine(
                    predicateIndex = { it.id == update.afterId },
                    items = update.filters
                )
            }
            is DVFilterUpdate.Move -> {
                filters.moveAfterIndexInLine(
                    predicateIndex = { filter -> filter.id == update.afterId },
                    predicateMove = { filter -> update.ids.contains(filter.id) }
                )
            }
            is DVFilterUpdate.Remove -> {
                filters.retainAll {
                    !update.ids.contains(it.id)
                }
            }
            is DVFilterUpdate.Update -> {
                filters.mapInPlace { filter ->
                    if (filter.id == update.id) update.filter else filter
                }
            }
        }
    }
    return filters
}

fun List<DVSort>.updateSorts(updates: List<DVSortUpdate>): List<DVSort> {
    val sorts = this.toMutableList()
    updates.forEach { update ->
        when (update) {
            is DVSortUpdate.Add -> {
                sorts.addAfterIndexInLine(
                    predicateIndex = { it.id == update.afterId },
                    items = update.sorts
                )
            }
            is DVSortUpdate.Move -> {
                sorts.moveAfterIndexInLine(
                    predicateIndex = { sort -> sort.id == update.afterId },
                    predicateMove = { sort -> update.ids.contains(sort.id) }
                )
            }
            is DVSortUpdate.Remove -> {
                sorts.retainAll {
                    !update.ids.contains(it.id)
                }
            }
            is DVSortUpdate.Update -> {
                sorts.mapInPlace { sort ->
                    if (sort.id == update.id) update.sort else sort
                }
            }
        }
    }
    return sorts
}

fun List<DVViewerRelation>.updateViewerRelations(updates: List<DVViewerRelationUpdate>): List<DVViewerRelation> {
    val relations = this.toMutableList()
    updates.forEach { update ->
        when (update) {
            is DVViewerRelationUpdate.Add -> {
                relations.addAfterIndexInLine(
                    predicateIndex = { it.key == update.afterId },
                    items = update.relations
                )
            }
            is DVViewerRelationUpdate.Move -> {
                if (update.afterId.isNotEmpty()) {
                    relations.moveAfterIndexInLine(
                        predicateIndex = { relation -> relation.key == update.afterId },
                        predicateMove = { relation -> update.ids.contains(relation.key) }
                    )
                } else {
                    relations.moveOnTop { update.ids.contains(it.key) }
                }
            }
            is DVViewerRelationUpdate.Remove -> {
                relations.retainAll {
                    !update.ids.contains(it.key)
                }
            }
            is DVViewerRelationUpdate.Update -> {
                relations.mapInPlace { relation ->
                    if (relation.key == update.id) update.relation else relation
                }
            }
        }
    }
    return relations
}

fun ObjectState.DataView.Set.getSetOfValue(ctx: Id): List<Id> {
    return ObjectWrapper.Basic(details[ctx]?.map.orEmpty()).setOf
}

fun ObjectState.DataView.filterOutDeletedAndMissingObjects(query: List<Id>): List<Id> {
    return query.filter(::isValidObject)
}

fun ObjectState.DataView.Set.isSetByRelation(setOfValue: List<Id>): Boolean {
    val objectDetails = details[setOfValue.first()]?.map.orEmpty()
    return objectDetails.type == ObjectTypeIds.RELATION
}

suspend fun StoreOfObjectTypes.isTemplatesAllowedForDefaultType(getDefaultPageType: GetDefaultPageType): Boolean {
    try {
        val defaultObjectType = getDefaultPageType.run(Unit).type ?: return false
        val defaultObjType = get(defaultObjectType) ?: return false
        return defaultObjType.isTemplatesAllowed()
    } catch (e: Exception) {
        return false
    }
}

private fun ObjectState.DataView.isValidObject(objectId: Id): Boolean {
    val objectDetails = details[objectId] ?: return false
    val objectWrapper = ObjectWrapper.Basic(objectDetails.map)
    return objectWrapper.isDeleted != true
}

fun DVViewer.updateFields(fields: DVViewerFields?): DVViewer {
    if (fields == null) return this
    return copy(
        name = fields.name,
        type = fields.type,
        coverRelationKey = fields.coverRelationKey,
        hideIcon = fields.hideIcon,
        cardSize = fields.cardSize,
        coverFit = fields.coverFit,
        defaultTemplate = fields.defaultTemplateId
    )
}

fun Viewer.isEmpty(): Boolean =
    when (this) {
        is Viewer.GalleryView -> this.items.isEmpty()
        is Viewer.GridView -> this.rows.isEmpty()
        is Viewer.ListView -> this.items.isEmpty()
        is Viewer.Unsupported -> false
    }

fun ObjectWrapper.Basic.toTemplateView(
    typeId: Id,
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
    objectTypeDefaultTemplate: Id? = null,
    viewerDefaultTemplate: Id? = null
): TemplateView.Template {
    val coverContainer = if (coverType != CoverType.NONE) {
        BasicObjectCoverWrapper(this)
            .getCover(urlBuilder, coverImageHashProvider)
    } else {
        null
    }
    val isDefault = if (viewerDefaultTemplate != null) {
        viewerDefaultTemplate == id
    } else {
        false
    }
    return TemplateView.Template(
        id = id,
        name = name.orEmpty(),
        typeId = typeId,
        emoji = if (!iconEmoji.isNullOrBlank()) iconEmoji else null,
        image = if (!iconImage.isNullOrBlank()) urlBuilder.thumbnail(iconImage!!) else null,
        layout = layout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = isDefault
    )
}

fun ObjectWrapper.Type.toTemplateViewBlank(
    viewerDefaultTemplate: Id? = null
): TemplateView.Blank {
    return TemplateView.Blank(
        id = DEFAULT_TEMPLATE_ID_BLANK,
        typeId = id,
        layout = recommendedLayout?.code ?: ObjectType.Layout.BASIC.code,
        isDefault = viewerDefaultTemplate == DEFAULT_TEMPLATE_ID_BLANK
    )
}

fun ObjectState.DataView.toViewersView(ctx: Id, session: ObjectSetSession): List<ViewerView> {
    val viewers = dataViewContent.viewers
    return when (this) {
        is ObjectState.DataView.Collection -> mapViewers(
            defaultObjectType = { it.defaultObjectType },
            viewers = viewers,
            session = session
        )
        is ObjectState.DataView.Set -> {
            val setOfValue = getSetOfValue(ctx)
            if (isSetByRelation(setOfValue = setOfValue)) {
                mapViewers(
                    defaultObjectType = { it.defaultObjectType },
                    viewers = viewers,
                    session = session
                )
            } else {
                mapViewers(
                    defaultObjectType = { setOfValue.firstOrNull() },
                    viewers = viewers,
                    session = session
                )
            }
        }
    }
}

private fun mapViewers(
    defaultObjectType: (DVViewer) -> Id?,
    viewers: List<DVViewer>,
    session: ObjectSetSession
): List<ViewerView> {
    return viewers.mapIndexed { index, viewer ->
        ViewerView(
            id = viewer.id,
            name = viewer.name,
            type = viewer.type,
            isActive = isActiveViewer(index, viewer, session),
            isUnsupported = viewer.type == VIEW_TYPE_UNSUPPORTED,
            defaultObjectType = defaultObjectType.invoke(viewer)
        )
    }
}

private fun isActiveViewer(index: Int, viewer: DVViewer, session: ObjectSetSession): Boolean {
    return if (session.currentViewerId.value != null) {
        viewer.id == session.currentViewerId.value
    } else {
        index == 0
    }
}

suspend fun List<ViewerView>.isActiveWithTemplates(storeOfObjectTypes: StoreOfObjectTypes): Boolean {
    val activeViewer = firstOrNull { it.isActive }
    val viewerDefaultObjectTypeId = activeViewer?.defaultObjectType ?: return false
    val viewerDefaultObjectType = storeOfObjectTypes.get(viewerDefaultObjectTypeId) ?: return false
    return viewerDefaultObjectType.isTemplatesAllowed()
}

fun ObjectState.DataView.Collection.getDefaultObjectTypeForCollection(viewer: DVViewer?): Id {
    return viewer?.defaultObjectType ?: VIEW_DEFAULT_OBJECT_TYPE
}

fun ObjectState.DataView.Set.getDefaultObjectTypeForSet(ctx: Id, viewer: DVViewer?): Id? {
    val setOfValue = getSetOfValue(ctx)
    return if (isSetByRelation(setOfValue = setOfValue)) {
        viewer?.defaultObjectType ?: VIEW_DEFAULT_OBJECT_TYPE
    } else {
        setOfValue.firstOrNull()
    }
}

fun ObjectState.DataView.getDefaultObjectType(ctx: Id, viewer: DVViewer?): Id? {
    return when (this) {
        is ObjectState.DataView.Collection -> getDefaultObjectTypeForCollection(viewer)
        is ObjectState.DataView.Set -> getDefaultObjectTypeForSet(ctx, viewer)
    }
}

//TODO Refact
suspend fun DVViewer.getProperTemplateId(
    templateId: Id?,
    storeOfObjectTypes: StoreOfObjectTypes
): Id? {
    return if (templateId != null) {
        if (templateId == DEFAULT_TEMPLATE_ID_BLANK) {
            null
        } else {
            templateId
        }
    } else {
        val defaultObjectTypeId = defaultObjectType
        if (defaultObjectTypeId != null) {
            storeOfObjectTypes.get(defaultObjectTypeId)?.defaultTemplateId
        } else {
            null
        }
    }
}