package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations


sealed class ViewerEditWidgetUi {
    object Init: ViewerEditWidgetUi()
    data class Data(
        val showWidget: Boolean = false,
        val showMore: Boolean = false,
        val isNewMode: Boolean = false,
        val id: Id,
        val name: String,
        val defaultObjectType: ObjectWrapper.Type?,
        val isDefaultObjectTypeEnabled: Boolean,
        val layout: DVViewerType?,
        val relations: List<Id> = emptyList(),
        val filters: List<Id> = emptyList(),
        val sorts: List<Id> = emptyList(),
        val defaultTemplateId: Id?,
        val defaultTemplateName: String?,
        val isActive: Boolean
    ) : ViewerEditWidgetUi()
}

sealed class ViewEditAction {
    object Dismiss : ViewEditAction()
    data class UpdateName(val id: Id, val name: String) : ViewEditAction()
    data class DefaultObjectType(val id: Id) : ViewEditAction()
    data class DefaultTemplate(val id: Id) : ViewEditAction()
    data class Layout(val id: Id) : ViewEditAction()
    data class Relations(val id: Id) : ViewEditAction()
    data class Filters(val id: Id) : ViewEditAction()
    data class Sorts(val id: Id) : ViewEditAction()
    object More: ViewEditAction()
    data class Duplicate(val id: Id) : ViewEditAction()
    data class Delete(val id: Id) : ViewEditAction()
}

suspend fun <T> List<T>.toView(
    storeOfRelations: StoreOfRelations,
    mapper: (T) -> String
): List<String> =
    mapNotNull {
        val relation = storeOfRelations.getByKey(mapper(it))
        relation?.name.orEmpty().takeIf { _ -> relation != null }
    }

suspend fun DVViewer.toViewerEditWidgetState(
    storeOfRelations: StoreOfRelations,
    storeOfObjectTypes: StoreOfObjectTypes,
    isDefaultObjectTypeEnabled: Boolean,
    details: Map<Id, Block.Fields>,
    index: Int,
    session: ObjectSetSession
): ViewerEditWidgetUi {
    val dvViewer = this
    val isActive = dvViewer.isActiveViewer(index, session)
    val viewerDefaultObjectTypeId = dvViewer.defaultObjectType ?: ObjectTypeIds.PAGE
    val viewerDefaultTemplateId = dvViewer.defaultTemplate
    val defaultObjectType = storeOfObjectTypes.get(viewerDefaultObjectTypeId)
    val defaultTemplateName = details[viewerDefaultTemplateId]?.name
    return ViewerEditWidgetUi.Data(
        showWidget = true,
        id = dvViewer.id,
        name = dvViewer.name,
        sorts = dvViewer.sorts.toView(storeOfRelations) { it.relationKey },
        filters = dvViewer.filters.toView(storeOfRelations) { it.relation },
        relations = dvViewer.viewerRelations.toView(storeOfRelations) { it.key },
        layout = dvViewer.type,
        defaultObjectType = defaultObjectType,
        isDefaultObjectTypeEnabled = isDefaultObjectTypeEnabled,
        defaultTemplateId = viewerDefaultTemplateId,
        defaultTemplateName = defaultTemplateName,
        isActive = isActive
    )
}

fun ViewerEditWidgetUi.isVisible(): Boolean =
    this is ViewerEditWidgetUi.Data && showWidget