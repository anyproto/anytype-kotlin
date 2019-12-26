package com.agileburo.anytype.presentation.databaseview.models

data class KanbanColumnView(
    val name: String,
    val rows: MutableList<KanbanRowView>
)