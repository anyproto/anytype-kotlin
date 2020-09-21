package com.agileburo.anytype.presentation.extension

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.desktop.DashboardView

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
    return this.sortedBy { orderedByIds[it.id] }
}

fun List<DashboardView>.filterByNotArchivedPages(): List<DashboardView> =
    this.filterNot { it is DashboardView.Profile || it.isArchived }

fun List<DashboardView>.updateDetails(
    target: String,
    details: Block.Fields,
    builder: UrlBuilder
): List<DashboardView> {
    return this.mapNotNull { view ->
        when (view) {
            is DashboardView.Profile -> {
                if (view.id == target) {
                    view.copy(
                        name = details.name.orEmpty(),
                        avatar = details.iconImage.let {
                            if (it.isNullOrEmpty()) null
                            else builder.image(it)
                        }
                    )
                } else {
                    view
                }
            }
            is DashboardView.Document -> {
                if (view.target == target) {
                    view.copy(
                        title = details.name,
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
                        isArchived = details.isArchived ?: false
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