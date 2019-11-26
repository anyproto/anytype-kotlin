package com.agileburo.anytype.presentation.contacts.model

data class ContactView(
    val id: String,
    val name: String,
    val date: Long,
    val icon: String?,
    val tags: List<TagView>?
)

data class TagView(
    val name: String
)