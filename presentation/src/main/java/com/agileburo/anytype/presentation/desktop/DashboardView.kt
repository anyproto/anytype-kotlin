package com.agileburo.anytype.presentation.desktop

import com.agileburo.anytype.domain.common.Id

sealed class DashboardView {

    abstract val id: Id

    data class Document(
        override val id: Id,
        val target: Id,
        val title: String? = null,
        val emoji: String? = null,
        val image: String? = null
    ) : DashboardView()

    data class Archive(
        override val id: Id,
        val target: Id,
        val text: String
    ) : DashboardView()
}