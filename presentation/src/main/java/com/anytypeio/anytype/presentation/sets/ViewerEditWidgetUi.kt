package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.StoreOfRelations


sealed class ViewerEditWidgetUi {
    object Init: ViewerEditWidgetUi()
    data class Data(
        val showWidget: Boolean = false,
        val showMore: Boolean = false,
        val isNewMode: Boolean = false,
        val id: Id,
        val name: String,
        val layout: DVViewerType?,
        val relations: List<Id> = emptyList(),
        val filters: List<Id> = emptyList(),
        val sorts: List<Id> = emptyList(),
        val isActive: Boolean
    ) : ViewerEditWidgetUi()
}

sealed class ViewEditAction {
    object Dismiss : ViewEditAction()
    data class UpdateName(val id: Id, val name: String) : ViewEditAction()
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
        val isRelationDoneOrName = relation?.key == Relations.DONE || relation?.key == Relations.NAME
        relation?.name.orEmpty()
            .takeIf { _ ->
                relation != null && relation.isValid
                        && (isRelationDoneOrName || relation.isHidden != true)
            }
    }

suspend fun List<DVViewerRelation>.toAppliedRelations(
    isGalleryViewer: Boolean = false,
    storeOfRelations: StoreOfRelations,
    mapper: (DVViewerRelation) -> Key
): List<String> =
    mapNotNull {
        val relation = storeOfRelations.getByKey(mapper(it))
        val filterByRelationKeys = if (!isGalleryViewer){
            relation?.key == Relations.DONE || relation?.key == Relations.NAME
        } else {
            relation?.key == Relations.DONE
        }
        relation?.name.orEmpty()
            .takeIf { _ ->
                relation != null && relation.isValid && (!filterByRelationKeys || relation.isHidden != true)
            }
    }


suspend fun DVViewer.toViewerEditWidgetState(
    storeOfRelations: StoreOfRelations,
    index: Int,
    session: ObjectSetSession
): ViewerEditWidgetUi {
    val dvViewer = this
    val isActive = dvViewer.isActiveViewer(index, session)
    return ViewerEditWidgetUi.Data(
        showWidget = true,
        id = dvViewer.id,
        name = dvViewer.name,
        sorts = dvViewer.sorts.toView(storeOfRelations) { it.relationKey },
        filters = dvViewer.filters.toView(storeOfRelations) { it.relation },
        relations = dvViewer.viewerRelations.filter { it.isVisible }.toAppliedRelations(
            isGalleryViewer = dvViewer.type == DVViewerType.GALLERY,
            storeOfRelations = storeOfRelations,
            mapper = { relation -> relation.key }
        ),
        layout = dvViewer.type,
        isActive = isActive
    )
}

fun ViewerEditWidgetUi.isVisible(): Boolean = this is ViewerEditWidgetUi.Data && showWidget