package com.agileburo.anytype.presentation.databaseview.models


sealed class KanbanRowView {

    abstract val id: String

    data class KanbanDemoView(
        override val id: String,
        val title: String,
        val assign: String,
        val tags: List<TagView>
    ) : KanbanRowView()

    data class KanbanFileView(
        override val id: String,
        val title: String,
        val category: String,
        val icon: String
    ) : KanbanRowView()

    data class KanbanPeopleView(
        override val id: String,
        val name: String,
        val category: String,
        val icon: String
    ) : KanbanRowView()

    data class KanbanBookmarkView(
        override val id: String,
        val title: String,
        val subtitle: String,
        val url: String,
        val logo: String,
        val image: String
    ) : KanbanRowView()

    data class KanbanPageView(
        override val id: String,
        val title: String,
        val icon: String,
        val category: String
    ) : KanbanRowView()

    data class KanbanTaskView(
        override val id: String,
        val title: String,
        val checked: Boolean,
        val category: String
    ) : KanbanRowView()

    data class KanbanAddNewItemView(override val id: String) : KanbanRowView()
}

