package com.agileburo.anytype.domain.database.model

enum class ViewType { GRID, BOARD, GALLERY, LIST }
enum class SortType { ASC, DESC }
enum class FilterTypeCondition { NONE, AND, OR }
enum class FilterTypeEquality { EQUAL, NOT_EQUAL, IN, NOT_IN, GREATER, LESSER, LIKE, NOT_LIKE }

data class Sort(
    val propertyId: String,
    val type: SortType
)

data class Filter(
    val propertyId: String,
    val condition: FilterTypeCondition,
    val equality: FilterTypeEquality,
    val value: Any
)

data class DisplayView(
    val id: String,
    val name: String,
    val type: ViewType,
    val sorts: List<Sort>,
    val filters: List<Filter>
)

data class ContentDatabaseView(
    val view: String,
    val properties: List<Property>,
    val displayViews: List<DisplayView>,
    val data: List<HashMap<String, Any>>
)

data class DatabaseView(val content: ContentDatabaseView)
