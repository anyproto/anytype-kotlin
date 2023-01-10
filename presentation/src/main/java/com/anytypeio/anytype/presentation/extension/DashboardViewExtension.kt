package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
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

suspend fun List<DashboardView>.updateDetails(
    target: String,
    details: Block.Fields,
    builder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes
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
                        typeName = obj.getTypeName(storeOfObjectTypes = storeOfObjectTypes)
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

/**
 * Considering type of Object, get name for this type by ObjectStore.
 * Could be type name, or deleted type name or unknown type name {see [DashboardView.TypeName]}
 */
suspend fun ObjectWrapper.Basic.getTypeName(objectStore: ObjectStore): DashboardView.TypeName {
    val type = type.firstOrNull()
    return if (type == null) {
        DashboardView.TypeName.Unknown
    } else {
        val typeObj = objectStore.get(type)
        val isDeleted = typeObj?.isDeleted ?: false
        if (isDeleted) {
            DashboardView.TypeName.Deleted
        } else {
            //todo Temporary workaround: type is considered deleted
            //if details are missing for this type or flag isDeleted equals to true
            if (typeObj?.id == null) {
                DashboardView.TypeName.Deleted
            } else {
                DashboardView.TypeName.Basic(name = typeObj.name)
            }
        }
    }
}

/**
 * Considering type of Object, get name for this type by StoreOfObjectTypes.
 * Could be type name, or deleted type name or unknown type name {see [DashboardView.TypeName]}
 */
suspend fun ObjectWrapper.Basic.getTypeName(storeOfObjectTypes: StoreOfObjectTypes): DashboardView.TypeName {
    val type = type.firstOrNull()
    return if (type == null) {
        DashboardView.TypeName.Unknown
    } else {
        val typeObj = storeOfObjectTypes.get(type)
        val isDeleted = typeObj?.isDeleted ?: false
        if (isDeleted) {
            DashboardView.TypeName.Deleted
        } else {
            //todo Temporary workaround: type is considered deleted
            //if details are missing for this type or flag isDeleted equals to true
            if (typeObj?.id == null) {
                DashboardView.TypeName.Deleted
            } else {
                DashboardView.TypeName.Basic(name = typeObj.name)
            }
        }
    }
}