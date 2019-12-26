package com.agileburo.anytype.domain.database.model

data class Contact(
    val id: String,
    val name: String,
    val date: Long,
    val icon: String?,
    val tags: List<Tag> = emptyList()
)

data class Tag(
    val id: String,
    val name: String
)
