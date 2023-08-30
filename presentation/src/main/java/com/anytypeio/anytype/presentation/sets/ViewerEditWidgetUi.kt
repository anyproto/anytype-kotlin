package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel

data class ViewerEditWidgetUi(
    val showWidget: Boolean,
    val name: String = "",
    val defaultObjectType: ObjectWrapper.Type?,
    val isDefaultObjectTypeEnabled: Boolean = false,
    val layout: ObjectType.Layout?,
    val relations: List<String> = emptyList(),
    val filters: List<String> = emptyList(),
    val sorts: List<String> = emptyList()
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
        data class UpdateDefaultObjectType(val type: ObjectWrapper.Type) : Action()
        data class UpdateLayout(val layout: ObjectType.Layout) : Action()
        data class UpdateRelations(val relations: List<String>) : Action()
        data class UpdateFilters(val filters: List<String>) : Action()
        data class UpdateSorts(val sorts: List<String>) : Action()
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