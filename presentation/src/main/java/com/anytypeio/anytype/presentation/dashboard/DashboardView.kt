package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class DashboardView {

    abstract val id: Id
    abstract val isArchived: Boolean
    abstract val isSelected: Boolean
    abstract val isLoading: Boolean

    data class Document(
        override val id: Id,
        val target: Id,
        val title: String = "",
        val emoji: String? = null,
        val image: String? = null,
        val layout: ObjectType.Layout? = null,
        val typeName: String? = null,
        val type: String? = null,
        val done: Boolean? = null,
        override val isArchived: Boolean,
        override val isSelected: Boolean = false,
        override val isLoading: Boolean = false,
        val icon: ObjectIcon = ObjectIcon.None
    ) : DashboardView() {
        val hasIcon = emoji != null || image != null
    }

    data class Archive(
        override val id: Id,
        val target: Id,
        val title: String,
        override val isArchived: Boolean = false,
        override val isSelected: Boolean = false,
        override val isLoading: Boolean = false
    ) : DashboardView()

    data class ObjectSet(
        override val id: Id,
        val target: Id,
        val title: String? = null,
        override val isArchived: Boolean,
        override val isSelected: Boolean = false,
        override val isLoading: Boolean = false,
        val icon: ObjectIcon = ObjectIcon.None
    ) : DashboardView()
}