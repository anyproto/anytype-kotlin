package com.agileburo.anytype.presentation.databaseview.models

enum class TableType { GRID, BOARD, GALLERY, LIST }

data class Table(
    val id: String,
    val column: List<Column>,
    val cell: List<List<Cell>>,
    val representations: List<Representation>
)

data class Representation(
    val id: String,
    val type: TableType,
    val name: String
//todo add filters and sorting
)

