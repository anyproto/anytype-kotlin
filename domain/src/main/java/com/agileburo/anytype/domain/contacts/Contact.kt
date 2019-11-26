package com.agileburo.anytype.domain.contacts

data class Contact(
    val id: String,
    val name: String,
    val date: Long,
    val icon: String?,
    val tags: List<Tag>?
)

data class Tag(
    val name: String
)
