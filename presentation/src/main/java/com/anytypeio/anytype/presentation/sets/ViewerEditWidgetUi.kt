package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView

data class ViewerEditWidgetUi(
    val showWidget: Boolean,
    val viewerView: ViewerView? = null,
    val name: String = "",
    val defaultObjectType: ObjectWrapper.Type?,
    val isDefaultObjectTypeEnabled: Boolean = false,
    val layout: DVViewerType?,
    val relations: List<String> = emptyList(),
    val filters: List<String> = emptyList(),
    val sorts: List<String> = emptyList(),
    val defaultTemplate: Id? = null
) {

    fun empty() = this.copy(
        name = "",
        defaultObjectType = null,
        isDefaultObjectTypeEnabled = false,
        layout = null,
        relations = emptyList(),
        filters = emptyList(),
        sorts = emptyList()
    )

    companion object {
        fun init() = ViewerEditWidgetUi(
            showWidget = false,
            defaultObjectType = null,
            isDefaultObjectTypeEnabled = false,
            layout = null
        )
    }

    sealed class Action {
        object Dismiss : Action()
        data class UpdateName(val name: String) : Action()
        object DefaultObjectType : Action()
        object Layout : Action()
        object Relations : Action()
        object Filters : Action()
        object Sorts : Action()
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
        name = dvViewer.name,
        sorts = dvViewer.sorts.toView(storeOfRelations) { it.relationKey },
        filters = dvViewer.filters.toView(storeOfRelations) { it.relation },
        relations = dvViewer.viewerRelations.toView (storeOfRelations) { it.key },
        layout = dvViewer.type,
        defaultObjectType = defaultObjectType,
        isDefaultObjectTypeEnabled = isDefaultObjectTypeEnabled,
        defaultTemplate = viewerDefaultTemplateId
    )
}