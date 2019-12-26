package com.agileburo.anytype.domain.database.model

import java.io.Serializable

enum class ViewType : Serializable { GRID, BOARD, GALLERY, LIST }
enum class SortType { ASC, DESC }
enum class FilterTypeCondition { NONE, AND, OR }
enum class FilterTypeEquality { EQUAL, NOT_EQUAL, IN, NOT_IN, GREATER, LESSER, LIKE, NOT_LIKE }

data class Sort(
    val detailId: String,
    val type: SortType
)

data class Filter(
    val detailId: String,
    val condition: FilterTypeCondition,
    val equality: FilterTypeEquality,
    val value: Any
)

data class Group(
    val details: List<Detail>
)

data class Display(
    val id: String,
    val name: String,
    val type: ViewType,
    val sorts: List<Sort> = emptyList(),
    val filters: List<Filter> = emptyList(),
    val groups: List<Group> = emptyList()
)

data class ContentDatabaseView(
    val databaseId: String,
    val details: List<Detail>,
    val displays: MutableList<Display>,
    val data: List<HashMap<String, Any>>
)

data class DatabaseView(val content: ContentDatabaseView)