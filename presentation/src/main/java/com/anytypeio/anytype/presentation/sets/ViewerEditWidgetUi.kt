package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations

data class ViewerEditWidgetUi(
    val showWidget: Boolean,
    val id: Id? = null,
    val name: String = "",
    val viewerId: Id,
    val defaultObjectType: ObjectWrapper.Type?,
    val isDefaultObjectTypeEnabled: Boolean = false,
    val layout: DVViewerType?,
    val relations: List<String> = emptyList(),
    val filters: List<String> = emptyList(),
    val sorts: List<String> = emptyList(),
    val defaultTemplate: Id? = null,
) {

    fun empty() = this.copy(
        id = null,
        name = "",
        layout = null,
        relations = emptyList(),
        filters = emptyList(),
        sorts = emptyList(),
        defaultTemplate = null,
        defaultObjectType = null,
        isDefaultObjectTypeEnabled = false,
    )

    companion object {
        fun init() = ViewerEditWidgetUi(
            showWidget = false,
            id = null,
            name = "",
            layout = null,
            relations = emptyList(),
            filters = emptyList(),
            sorts = emptyList(),
            defaultTemplate = null,
            defaultObjectType = null,
            isDefaultObjectTypeEnabled = false,
            viewerId = "",
        )
    }

    sealed class Action {
        object Dismiss : Action()
        data class UpdateName(val id: Id?, val name: String) : Action()
        data class DefaultObjectType(val id: Id?) : Action()
        data class Layout(val id: Id?) : Action()
        data class Relations(val id: Id?) : Action()
        data class Filters(val id: Id?) : Action()
        data class Sorts(val id: Id?) : Action()
        object More: Action()
    }
}

suspend fun <T> List<T>.toView(
    storeOfRelations: StoreOfRelations,
    mapper: (T) -> String
): List<String> =
    mapNotNull {
        val relation = storeOfRelations.getByKey(mapper(it))
        relation?.name.orEmpty().takeIf { _ -> relation != null }
    }

suspend fun ViewerEditWidgetUi.updateState(
    dvViewer: DVViewer,
    storeOfRelations: StoreOfRelations,
    storeOfObjectTypes: StoreOfObjectTypes,
    isDefaultObjectTypeEnabled: Boolean
): ViewerEditWidgetUi {

    val viewerDefaultObjectTypeId = dvViewer.defaultObjectType ?: ObjectTypeIds.PAGE
    val viewerDefaultTemplateId = dvViewer.defaultTemplate
    val defaultObjectType = storeOfObjectTypes.get(viewerDefaultObjectTypeId)
    return this.copy(
        id = dvViewer.id,
        name = dvViewer.name,
        sorts = dvViewer.sorts.toView(storeOfRelations) { it.relationKey },
        filters = dvViewer.filters.toView(storeOfRelations) { it.relation },
        relations = dvViewer.viewerRelations.toView(storeOfRelations) { it.key },
        layout = dvViewer.type,
        defaultObjectType = defaultObjectType,
        isDefaultObjectTypeEnabled = isDefaultObjectTypeEnabled,
        defaultTemplate = viewerDefaultTemplateId
    )
}