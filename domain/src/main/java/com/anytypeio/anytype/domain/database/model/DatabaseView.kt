package com.anytypeio.anytype.domain.database.model

import java.io.Serializable

@Deprecated("Legacy")
enum class ViewType : Serializable { GRID, BOARD, GALLERY, LIST }
@Deprecated("Legacy")
enum class FilterTypeCondition { NONE, AND, OR }
@Deprecated("Legacy")
enum class FilterTypeEquality { EQUAL, NOT_EQUAL, IN, NOT_IN, GREATER, LESSER, LIKE, NOT_LIKE }

@Deprecated("Legacy")
data class Filter(
    val detailId: String,
    val condition: FilterTypeCondition,
    val equality: FilterTypeEquality,
    val value: Any
)

@Deprecated("Legacy")
data class Group(
    val details: List<Detail>
)

@Deprecated("Legacy")
data class Display(
    val id: String,
    val name: String,
    val type: ViewType,
    val filters: List<Filter> = emptyList(),
    val groups: List<Group> = emptyList()
)

@Deprecated("Legacy")
data class ContentDatabaseView(
    val databaseId: String,
    val details: List<Detail>,
    val displays: MutableList<Display>,
    val data: List<HashMap<String, Any>>
)

@Deprecated("Legacy")
data class DatabaseView(val content: ContentDatabaseView)