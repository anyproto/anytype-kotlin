package com.agileburo.anytype.presentation.desktop

import com.agileburo.anytype.domain.common.Id

sealed class DashboardView {
    data class Document(
        val id: Id,
        val target: Id,
        val title: String? = null,
        val emoji: String? = null,
        val image: String? = null
    ) : DashboardView()
}