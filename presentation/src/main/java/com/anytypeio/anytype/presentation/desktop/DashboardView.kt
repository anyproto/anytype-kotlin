package com.anytypeio.anytype.presentation.desktop

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Url

sealed class DashboardView {

    abstract val id: Id
    abstract val isArchived: Boolean
    abstract val isLoading: Boolean

    data class Profile(
        override val id: Id,
        val name: String,
        val avatar: Url? = null,
        override val isArchived: Boolean = false,
        override val isLoading: Boolean = false
    ) : DashboardView()

    data class Document(
        override val id: Id,
        val target: Id,
        val title: String? = null,
        val emoji: String? = null,
        val image: String? = null,
        val layout: ObjectType.Layout? = null,
        val typeName: String? = null,
        val type: String? = null,
        override val isArchived: Boolean,
        override val isLoading: Boolean = false
    ) : DashboardView() {
        val hasIcon = emoji != null || image != null
    }

    data class Archive(
        override val id: Id,
        val target: Id,
        val title: String,
        override val isArchived: Boolean = false,
        override val isLoading: Boolean = false
    ) : DashboardView()

    data class ObjectSet(
        override val id: Id,
        val target: Id,
        val title: String? = null,
        val emoji: String? = null,
        override val isArchived: Boolean,
        override val isLoading: Boolean = false
    ) : DashboardView()
}