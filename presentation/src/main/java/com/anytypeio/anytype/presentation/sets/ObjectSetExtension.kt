package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CoverType
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterQuickOption
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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ext.addAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.mapInPlace
import com.anytypeio.anytype.core_utils.ext.moveAfterIndexInLine
import com.anytypeio.anytype.core_utils.ext.moveOnTop
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.relations.BasicObjectCoverWrapper
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.ID_KEY
import com.anytypeio.anytype.presentation.relations.getCover
import com.anytypeio.anytype.presentation.relations.isSystemKey
import com.anytypeio.anytype.presentation.relations.linksFeaturedRelation
import com.anytypeio.anytype.presentation.relations.title
import com.anytypeio.anytype.presentation.relations.view
import com.anytypeio.anytype.core_models.PermittedConditions
import com.anytypeio.anytype.core_models.ext.DAYS_IN_MONTH
import com.anytypeio.anytype.core_models.ext.DAYS_IN_WEEK
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.ext.SECONDS_IN_DAY
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_TYPE_UNSUPPORTED
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import com.anytypeio.anytype.presentation.templates.TemplateView
import timber.log.Timber

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
            val objectType = details.details[typeId]?.map?.mapToObjectWrapperType()
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
        Relations.BACKLINKS, Relations.LINKS -> {
            details.linksFeaturedRelation(
                relations = relations,
                ctx = ctx,
                relationKey = key,
                isFeatured = true
            )
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
    .sortedByDescending { it.key == Relations.SET_OF }
    .sortedByDescending { it.key == Relations.TYPE }

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

fun ObjectState.DataView.viewerByIdOrFirst(currentViewerId: String?): DVViewer? {
    if (!isInitialized) return null
    return dataViewContent.viewers.find { it.id == currentViewerId }
        ?: dataViewContent.viewers.firstOrNull()
}

fun ObjectState.DataView.viewerById(currentViewerId: String?): DVViewer? {
    if (!isInitialized) return null
    return dataViewContent.viewers.find { it.id == currentViewerId }
}

fun ObjectState.DataView.viewerAndIndexById(currentViewerId: String?): Pair<DVViewer, Int>? {
    if (!isInitialized) return null
    val index = dataViewContent.viewers.indexOfFirst { it.id == currentViewerId }
    return if (index != -1) {
        Pair(dataViewContent.viewers[index], index)
    } else {
        null
    }
}

fun ObjectState.DataView.Collection.getObjectOrderIds(currentViewerId: String): List<Id> {
    return dataViewContent.objectOrders.find { it.view == currentViewerId }?.ids ?: emptyList()
}

fun List<DVFilter>.updateFormatForSubscription(relationLinks: List<RelationLink>): List<DVFilter> {
    return map { f: DVFilter ->
        val r = relationLinks.firstOrNull { it.key == f.relation }
        if (r != null && r.format == RelationFormat.DATE) {
            f.copy(relationFormat = r.format)
        } else if (r != null && r.format == RelationFormat.OBJECT) {
            // Temporary workaround for normalizing filter condition for object filters
            f.copy(
                relationFormat = r.format,
                condition = if (f.condition == DVFilterCondition.EQUAL) {
                    DVFilterCondition.IN
                } else {
                    f.condition
                }
            )
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
        isRelation = layout == ObjectType.Layout.RELATION
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
    if (setOfValue.isEmpty()) return false
    val objectDetails = details[setOfValue.first()]?.map.orEmpty()
    val wrapper = ObjectWrapper.Basic(objectDetails)
    return wrapper.layout == ObjectType.Layout.RELATION
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
        defaultTemplate = fields.defaultTemplateId,
        defaultObjectType = fields.defaultObjectTypeId
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
    urlBuilder: UrlBuilder,
    coverImageHashProvider: CoverImageHashProvider,
    viewerDefTemplateId: Id?,
    viewerDefTypeKey: TypeKey
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
        image = if (!iconImage.isNullOrBlank()) urlBuilder.thumbnail(iconImage!!) else null,
        layout = layout ?: ObjectType.Layout.BASIC,
        coverColor = coverContainer?.coverColor,
        coverImage = coverContainer?.coverImage,
        coverGradient = coverContainer?.coverGradient,
        isDefault = viewerDefTemplateId == id,
        targetTypeKey = viewerDefTypeKey
    )
}

suspend fun ObjectState.DataView.toViewersView(ctx: Id, session: ObjectSetSession, storeOfRelations: StoreOfRelations): List<ViewerView> {
    val viewers = dataViewContent.viewers
    return when (this) {
        is ObjectState.DataView.Collection -> mapViewers(
            defaultObjectType = { it.defaultObjectType },
            viewers = viewers,
            session = session,
            storeOfRelations = storeOfRelations
        )
        is ObjectState.DataView.Set -> {
            val setOfValue = getSetOfValue(ctx)
            if (isSetByRelation(setOfValue = setOfValue)) {
                mapViewers(
                    defaultObjectType = { it.defaultObjectType },
                    viewers = viewers,
                    session = session,
                    storeOfRelations = storeOfRelations
                )
            } else {
                mapViewers(
                    defaultObjectType = { setOfValue.firstOrNull() },
                    viewers = viewers,
                    session = session,
                    storeOfRelations = storeOfRelations
                )
            }
        }
    }
}

private suspend fun mapViewers(
    defaultObjectType: (DVViewer) -> Id?,
    viewers: List<DVViewer>,
    session: ObjectSetSession,
    storeOfRelations: StoreOfRelations
): List<ViewerView> {
    return viewers.mapIndexed { index, viewer ->
        ViewerView(
            id = viewer.id,
            name = viewer.name,
            type = viewer.type,
            isUnsupported = viewer.type == VIEW_TYPE_UNSUPPORTED,
            isActive = viewer.isActiveViewer(index, session),
            defaultObjectType = defaultObjectType.invoke(viewer),
            relations = viewer.viewerRelations.toView(storeOfRelations) { it.key },
            sorts = viewer.sorts.toView(storeOfRelations) { it.relationKey },
            filters = viewer.filters.toView(storeOfRelations) { it.relation },
            isDefaultObjectTypeEnabled = true
        )
    }
}

fun DVViewer.isActiveViewer(index: Int, session: ObjectSetSession): Boolean {
    return if (session.currentViewerId.value != null) {
        id == session.currentViewerId.value
    } else {
        index == 0
    }
}

suspend fun ObjectState.DataView.getActiveViewTypeAndTemplate(
    ctx: Id,
    activeView: DVViewer?,
    storeOfObjectTypes: StoreOfObjectTypes
): Pair<ObjectWrapper.Type?, Id?> {
    if (activeView == null) return Pair(null, null)
    when (this) {
        is ObjectState.DataView.Collection -> {
            return resolveTypeAndActiveViewTemplate(activeView, storeOfObjectTypes)
        }
        is ObjectState.DataView.Set -> {
            val setOfValue = getSetOfValue(ctx)
            return if (isSetByRelation(setOfValue = setOfValue)) {
                resolveTypeAndActiveViewTemplate(activeView, storeOfObjectTypes)
            } else {
                val setOf = setOfValue.firstOrNull()
                if (setOf.isNullOrBlank()) {
                    Timber.d("Set by type setOf param is null or empty, not possible to get Type and Template")
                    Pair(null, null)
                } else {
                    val defaultSetObjectType = details[setOf]?.map?.mapToObjectWrapperType()
                    if (activeView.defaultTemplate.isNullOrEmpty()) {
                        val defaultTemplateId = defaultSetObjectType?.defaultTemplateId
                        Pair(defaultSetObjectType, defaultTemplateId)
                    } else {
                        Pair(defaultSetObjectType, activeView.defaultTemplate)
                    }
                }
            }
        }
    }
}

private suspend fun resolveTypeAndActiveViewTemplate(
    activeView: DVViewer,
    storeOfObjectTypes: StoreOfObjectTypes
): Pair<ObjectWrapper.Type?, Id?> {
    val activeViewDefaultObjectType = activeView.defaultObjectType
    val defaultSetObjectTypId = if (!activeViewDefaultObjectType.isNullOrBlank()) {
        activeViewDefaultObjectType
    } else {
        VIEW_DEFAULT_OBJECT_TYPE
    }
    val defaultSetObjectType = storeOfObjectTypes.get(defaultSetObjectTypId) ?: storeOfObjectTypes.getByKey(defaultSetObjectTypId)
    return if (activeView.defaultTemplate.isNullOrEmpty()) {
        val defaultTemplateId = defaultSetObjectType?.defaultTemplateId
        Pair(defaultSetObjectType, defaultTemplateId)
    } else {
        Pair(defaultSetObjectType, activeView.defaultTemplate)
    }
}

fun ObjectState.DataView.isChangingDefaultTypeAvailable(): Boolean {
    return when (this) {
        is ObjectState.DataView.Collection -> true
        is ObjectState.DataView.Set -> {
            val setOfValue = getSetOfValue(root)
            isSetByRelation(setOfValue = setOfValue)
        }
    }
}

suspend fun DVViewer.prefillNewObjectDetails(
    storeOfRelations: StoreOfRelations,
    dateProvider: DateProvider,
    dataViewRelationLinks: List<RelationLink>
): Struct =
    buildMap {
        filters.forEach { filter ->
            val relationObject = storeOfRelations.getByKey(filter.relation) ?: return@forEach
            if (!relationObject.isReadonlyValue && PermittedConditions.contains(
                    filter.condition
                )
            ) {
                //Relation format should be taken from DataView relation links
                val filterRelationFormat =
                    dataViewRelationLinks.firstOrNull { it.key == filter.relation }?.format
                when (filterRelationFormat) {
                    Relation.Format.DATE -> {
                        val value = DateParser.parse(filter.value)
                        val updatedValue = filter.quickOption.getTimestampForQuickOption(
                            value = value,
                            dateProvider = dateProvider
                        )
                        if (updatedValue != null) {
                            put(filter.relation, updatedValue.toDouble())
                        }
                    }
                    else -> {
                        filter.value?.let { put(filter.relation, it) }
                    }
                }
            }
        }
    }

suspend fun DVViewer.resolveSetByRelationPrefilledObjectData(
    storeOfRelations: StoreOfRelations,
    dateProvider: DateProvider,
    objSetByRelation: ObjectWrapper.Relation,
    dataViewRelationLinks: List<RelationLink>
): Struct {
    val prefillWithSetOf = buildMap {
        val relationFormat = objSetByRelation.relationFormat
        val defaultValue = resolveDefaultValueByFormat(relationFormat)
        put(objSetByRelation.key, defaultValue)
    }
    val prefillNewObjectDetails = prefillNewObjectDetails(storeOfRelations, dateProvider, dataViewRelationLinks)
    return prefillWithSetOf + prefillNewObjectDetails
}

private fun resolveDefaultValueByFormat(format: RelationFormat): Any? = when (format) {
    Relation.Format.LONG_TEXT,
    Relation.Format.SHORT_TEXT,
    Relation.Format.URL,
    Relation.Format.EMAIL,
    Relation.Format.PHONE,
    Relation.Format.EMOJI -> EMPTY_STRING_VALUE
    Relation.Format.CHECKBOX -> false
    Relation.Format.NUMBER -> null
    else -> null
}

fun DVFilterQuickOption.getTimestampForQuickOption(value: Long?, dateProvider: DateProvider): Long? {
    val option = this
    val time = dateProvider.getCurrentTimestampInSeconds()
    return when (option) {
        DVFilterQuickOption.DAYS_AGO -> {
            if (value == null) return null
            time - SECONDS_IN_DAY * value
        }
        DVFilterQuickOption.LAST_MONTH -> time - SECONDS_IN_DAY * DAYS_IN_MONTH
        DVFilterQuickOption.LAST_WEEK -> time - SECONDS_IN_DAY * DAYS_IN_WEEK
        DVFilterQuickOption.YESTERDAY -> time - SECONDS_IN_DAY
        DVFilterQuickOption.CURRENT_WEEK,
        DVFilterQuickOption.CURRENT_MONTH,
        DVFilterQuickOption.TODAY -> time
        DVFilterQuickOption.TOMORROW -> time + SECONDS_IN_DAY
        DVFilterQuickOption.NEXT_WEEK -> time + SECONDS_IN_DAY * DAYS_IN_WEEK
        DVFilterQuickOption.NEXT_MONTH -> time + SECONDS_IN_DAY * DAYS_IN_MONTH
        DVFilterQuickOption.DAYS_AHEAD -> {
            if (value == null) return null
            time + SECONDS_IN_DAY * value
        }
        DVFilterQuickOption.EXACT_DATE -> value
    }
}

