package com.agileburo.anytype.presentation.databaseview.models

data class ListItem(
    val id: String,
    val name: String,
    val date: Long,
    val icon: String?,
    val tags: List<TagView> = emptyList()
)

data class TagView(
    val id: String,
    val name: String
)