package com.anytypeio.anytype.presentation.desktop

import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.common.Url

sealed class DashboardView {

    abstract val id: Id
    abstract val isArchived: Boolean

    data class Profile(
        override val id: Id,
        val name: String,
        val avatar: Url? = null,
        override val isArchived: Boolean = false
    ) : DashboardView()

    data class Document(
        override val id: Id,
        val target: Id,
        val title: String? = null,
        val emoji: String? = null,
        val image: String? = null,
        override val isArchived: Boolean
    ) : DashboardView()

    data class Archive(
        override val id: Id,
        val target: Id,
        val title: String,
        override val isArchived: Boolean = false
    ) : DashboardView()
}