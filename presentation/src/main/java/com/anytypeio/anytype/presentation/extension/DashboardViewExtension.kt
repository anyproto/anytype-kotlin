package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.dashboard.DashboardView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName

fun List<DashboardView>.addAndSortByIds(
    ids: List<String>,
    new: List<DashboardView>
): List<DashboardView> = toMutableList().let { list ->
    list.addAll(new)
    list.sortByIds(ids)
}

fun List<DashboardView>.sortByIds(
    ids: List<String>
): List<DashboardView> {
    val orderedByIds = ids.withIndex().associate { it.value to it.index }
    return this.sortedBy { orderedByIds[it.id] }.filter { ids.contains(it.id) }
}

fun List<DashboardView>.filterByNotArchivedPages(): List<DashboardView> =
    this.filterNot { it.isArchived }

fun List<DashboardView>.updateDetails(
    target: String,
    details: Block.Fields,
    builder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<DashboardView> {
    return mapNotNull { view ->
        when (view) {
            is DashboardView.Document -> {
                if (view.target == target) {
                    val obj = ObjectWrapper.Basic(details.map)
                    if (obj.isDeleted == true)
                        null
                    else
                        view.copy(
                        title = obj.getProperName(),
                        emoji = details.iconEmoji?.let { name ->
                            if (name.isNotEmpty())
                                name
                            else
                                null
                        },
                        image = details.iconImage?.let { name ->
                            if (name.isNotEmpty())
                                builder.image(name)
                            else
                                null
                        },
                        isArchived = details.isArchived ?: false,
                        isLoading = false,
                        icon = ObjectIcon.from(
                            obj = obj,
                            layout = obj.layout,
                            builder = builder
                        ),
                        layout = obj.layout,
                        type = obj.type.firstOrNull(),
                        typeName = objectTypes.find { it.id == obj.type.firstOrNull() }?.name
                    )
                } else {
                    view
                }
            }
            is DashboardView.ObjectSet -> {
                if (view.target == target) {
                    val obj = ObjectWrapper.Basic(details.map)
                    if (obj.isDeleted == true)
                        null
                    else
                        view.copy(
                            title = details.name,
                            isArchived = details.isArchived ?: false,
                            icon = ObjectIcon.from(
                                obj = obj,
                                layout = obj.layout,
                                builder = builder
                            )
                        )
                } else {
                    view
                }
            }
            is DashboardView.Archive -> {
                if (view.id == target) {
                    view.copy(
                        title = details.name.orEmpty()
                    )
                } else {
                    view
                }
            }
        }
    }
}